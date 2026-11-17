package com.vsblog.project;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.vsblog.article.Article;
import com.vsblog.article.ArticleRepository;
import com.vsblog.article.ArticleStatus;
import com.vsblog.article.ArticleVisibility;
import com.vsblog.util.SlugGenerator;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ArticleRepository articleRepository;

    public ProjectService(ProjectRepository projectRepository, ArticleRepository articleRepository) {
        this.projectRepository = projectRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listPublic() {
        Sort sort = Sort.by(Sort.Order.desc("featured"), Sort.Order.asc("sortOrder"), Sort.Order.desc("createdAt"));
        return projectRepository.findByStatusAndVisibility(ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC, sort)
                .stream()
                .map(project -> toResponse(project, true))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getPublicBySlug(String slug) {
        Project project = projectRepository.findBySlug(slug).orElseThrow(() -> new ProjectNotFoundException(slug));
        if (project.getStatus() != ArticleStatus.PUBLISHED || project.getVisibility() != ArticleVisibility.PUBLIC) {
            throw new ProjectNotFoundException(slug);
        }
        return toResponse(project, true);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> listManaged() {
        return projectRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream().map(project -> toResponse(project, false)).toList();
    }

    @Transactional(readOnly = true)
    public ProjectResponse getById(UUID id) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        return toResponse(project, false);
    }

    @Transactional
    public ProjectResponse create(ProjectRequest request) {
        String slug = normalizeSlug(request.slug(), request.title());
        ensureUniqueSlug(slug, null);
        validateDates(request.startedAt(), request.completedAt());

        Project project = new Project();
        applyRequest(project, request, slug, false);
        return toResponse(projectRepository.save(project), false);
    }

    @Transactional
    public ProjectResponse update(UUID id, ProjectRequest request) {
        Project project = projectRepository.findById(id).orElseThrow(() -> new ProjectNotFoundException(id));
        String slug = request.slug() == null || request.slug().isBlank()
                ? project.getSlug()
                : normalizeSlug(request.slug(), request.title());
        ensureUniqueSlug(slug, id);
        validateDates(request.startedAt(), request.completedAt());

        applyRequest(project, request, slug, true);
        return toResponse(projectRepository.save(project), false);
    }

    @Transactional
    public void delete(UUID id) {
        if (!projectRepository.existsById(id)) {
            throw new ProjectNotFoundException(id);
        }
        projectRepository.deleteById(id);
    }

    private void applyRequest(Project project, ProjectRequest request, String slug, boolean update) {
        project.setTitle(request.title().trim());
        project.setSlug(slug);
        project.setSummary(request.summary().trim());
        project.setDescriptionMarkdown(request.descriptionMarkdown());
        project.setTechStack(request.techStack() == null
                ? update ? project.getTechStack() : List.of()
                : List.copyOf(request.techStack()));
        project.setProjectUrl(request.projectUrl());
        project.setRepositoryUrl(request.repositoryUrl());
        project.setCoverImageUrl(request.coverImageUrl());
        project.setGallery(request.gallery() == null
                ? update ? project.getGallery() : List.of()
                : List.copyOf(request.gallery()));
        project.setRole(request.role());
        project.setStatus(request.status() == null
                ? update ? project.getStatus() : ArticleStatus.DRAFT
                : request.status());
        project.setVisibility(request.visibility() == null
                ? update ? project.getVisibility() : ArticleVisibility.PRIVATE
                : request.visibility());
        project.setFeatured(request.featured() == null
                ? update && project.isFeatured()
                : request.featured());
        project.setSortOrder(request.sortOrder() == null
                ? update ? project.getSortOrder() : 0
                : request.sortOrder());
        project.setStartedAt(request.startedAt());
        project.setCompletedAt(request.completedAt());
        project.setSeoTitle(request.seoTitle());
        project.setSeoDescription(request.seoDescription());
        if (request.articleIds() != null || !update) {
            project.setArticles(resolveArticles(request.articleIds()));
        }
    }

    private List<Article> resolveArticles(List<UUID> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            return new ArrayList<>();
        }
        List<UUID> uniqueIds = articleIds.stream().distinct().toList();
        List<Article> articles = articleRepository.findAllById(uniqueIds);
        if (articles.size() != uniqueIds.size()) {
            throw new IllegalArgumentException("One or more articles were not found");
        }
        return new ArrayList<>(articles);
    }

    private void validateDates(java.time.LocalDate startedAt, java.time.LocalDate completedAt) {
        if (startedAt != null && completedAt != null && completedAt.isBefore(startedAt)) {
            throw new IllegalArgumentException("Project completion date cannot precede its start date");
        }
    }

    private void ensureUniqueSlug(String slug, UUID ignoredProjectId) {
        boolean exists = ignoredProjectId == null
                ? projectRepository.existsBySlug(slug)
                : projectRepository.existsBySlugAndIdNot(slug, ignoredProjectId);
        if (exists) {
            throw new DuplicateProjectSlugException(slug);
        }
    }

    private String normalizeSlug(String value, String fallback) {
        return SlugGenerator.generate(value, fallback);
    }

    private ProjectResponse toResponse(Project project, boolean publicView) {
        List<UUID> articleIds = project.getArticles().stream()
                .filter(article -> !publicView || (article.getStatus() == ArticleStatus.PUBLISHED
                        && article.getVisibility() == ArticleVisibility.PUBLIC))
                .map(Article::getId)
                .sorted()
                .toList();
        return new ProjectResponse(
                project.getId(),
                project.getTitle(),
                project.getSlug(),
                project.getSummary(),
                project.getDescriptionMarkdown(),
                List.copyOf(project.getTechStack()),
                project.getProjectUrl(),
                project.getRepositoryUrl(),
                project.getCoverImageUrl(),
                List.copyOf(project.getGallery()),
                project.getRole(),
                project.getStatus(),
                project.getVisibility(),
                project.isFeatured(),
                project.getSortOrder(),
                project.getStartedAt(),
                project.getCompletedAt(),
                project.getSeoTitle(),
                project.getSeoDescription(),
                articleIds,
                project.getCreatedAt(),
                project.getUpdatedAt());
    }
}
