package com.vsblog.article;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.vsblog.category.Category;
import com.vsblog.category.CategoryRepository;
import com.vsblog.util.SlugGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public ArticleService(ArticleRepository articleRepository, CategoryRepository categoryRepository, TagRepository tagRepository) {
        this.articleRepository = articleRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> listPublishedPublic() {
        return articleRepository.findPublishedPublicOrdered(ArticleVisibility.PUBLIC, ArticleStatus.PUBLISHED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponse> listPublishedPublicPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt"));
        return articleRepository.findPublishedPublicPage(ArticleVisibility.PUBLIC, ArticleStatus.PUBLISHED, pageable)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ArticleResponse> searchPublic(String query, int page, int size) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }
        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException("Page must be non-negative and size must be between 1 and 100");
        }

        return articleRepository.searchPublicByText(query.trim(), PageRequest.of(page, size))
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ArticleResponse getPublicBySlug(String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new ArticleNotFoundException(slug));
        if (article.getVisibility() != ArticleVisibility.PUBLIC || article.getStatus() != ArticleStatus.PUBLISHED) {
            throw new ArticleNotFoundException(slug);
        }
        return toResponse(article);
    }

    @Transactional(readOnly = true)
    public List<ArticleResponse> listManaged() {
        return articleRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ArticleResponse getById(UUID id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));
        return toResponse(article);
    }

    @Transactional
    public ArticleResponse create(ArticleRequest request) {
        Category category = resolveCategory(request.categoryId());
        String slug = normalizeSlug(request.slug(), request.title());
        ensureUniqueSlug(slug, null);
        ArticleStatus status = request.status() == null ? ArticleStatus.DRAFT : request.status();
        ArticleVisibility visibility = request.visibility() == null ? ArticleVisibility.PRIVATE : request.visibility();

        Article article = new Article(
                request.title().trim(),
                slug,
                request.summary(),
                request.contentMarkdown(),
                category,
                status,
                visibility,
                request.coverImageUrl(),
                Boolean.TRUE.equals(request.pinned()),
                request.seoTitle(),
                request.seoDescription(),
                resolvePublishedAt(status)
        );
        article.setTags(resolveTags(request.tagIds()));

        return toResponse(articleRepository.save(article));
    }

    @Transactional
    public ArticleResponse update(UUID id, ArticleRequest request) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException(id));

        Category category = resolveCategory(request.categoryId());
        String slug = request.slug() == null || request.slug().isBlank()
                ? article.getSlug()
                : normalizeSlug(request.slug(), request.title());
        ensureUniqueSlug(slug, id);

        article.setTitle(request.title().trim());
        article.setSlug(slug);
        article.setSummary(request.summary());
        article.setContentMarkdown(request.contentMarkdown());
        article.setCategory(category);
        article.setStatus(request.status() == null ? article.getStatus() : request.status());
        article.setVisibility(request.visibility() == null ? article.getVisibility() : request.visibility());
        article.setCoverImageUrl(request.coverImageUrl());
        article.setPinned(request.pinned() == null ? article.isPinned() : request.pinned());
        article.setSeoTitle(request.seoTitle());
        article.setSeoDescription(request.seoDescription());
        article.setTags(resolveTags(request.tagIds()));
        if (article.getStatus() == ArticleStatus.PUBLISHED && article.getPublishedAt() == null) {
            article.setPublishedAt(Instant.now());
        }

        return toResponse(articleRepository.save(article));
    }

    @Transactional
    public void delete(UUID id) {
        if (!articleRepository.existsById(id)) {
            throw new ArticleNotFoundException(id);
        }
        articleRepository.deleteById(id);
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + categoryId));
    }

    private Set<Tag> resolveTags(List<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<Tag> tags = tagRepository.findAllByIdIn(tagIds);
        if (tags.size() != new HashSet<>(tagIds).size()) {
            throw new IllegalArgumentException("One or more tags were not found");
        }
        return tags;
    }

    private void ensureUniqueSlug(String slug, UUID ignoredArticleId) {
        boolean exists = ignoredArticleId == null
                ? articleRepository.existsBySlug(slug)
                : articleRepository.existsBySlugAndIdNot(slug, ignoredArticleId);
        if (exists) {
            throw new DuplicateArticleSlugException(slug);
        }
    }

    private String normalizeSlug(String value, String fallback) {
        return SlugGenerator.generate(value, fallback);
    }

    private Instant resolvePublishedAt(ArticleStatus status) {
        if (status == ArticleStatus.PUBLISHED) {
            return Instant.now();
        }
        return null;
    }

    private ArticleResponse toResponse(Article article) {
        return new ArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getSlug(),
                article.getSummary(),
                article.getContentMarkdown(),
                article.getCategory() == null ? null : article.getCategory().getId(),
                article.getStatus(),
                article.getVisibility(),
                article.getCoverImageUrl(),
                article.isPinned(),
                article.getSeoTitle(),
                article.getSeoDescription(),
                article.getPublishedAt(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                article.getTags().stream().map(Tag::getId).sorted().toList());
    }
}
