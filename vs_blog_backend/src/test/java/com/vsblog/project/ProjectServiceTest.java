package com.vsblog.project;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.vsblog.article.Article;
import com.vsblog.article.ArticleRepository;
import com.vsblog.article.ArticleStatus;
import com.vsblog.article.ArticleVisibility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ProjectService projectService;

    @Test
    void publicListingQueriesOnlyPublishedPublicProjectsWithStableOrdering() {
        given(projectRepository.findByStatusAndVisibility(
                eq(ArticleStatus.PUBLISHED), eq(ArticleVisibility.PUBLIC), org.mockito.ArgumentMatchers.any(Sort.class)))
                .willReturn(List.of());

        projectService.listPublic();

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(projectRepository).findByStatusAndVisibility(
                eq(ArticleStatus.PUBLISHED), eq(ArticleVisibility.PUBLIC), sortCaptor.capture());
        assertEquals(Sort.by(Sort.Order.desc("featured"), Sort.Order.asc("sortOrder"),
                Sort.Order.desc("createdAt")), sortCaptor.getValue());
    }

    @Test
    void publicDetailHidesDraftProject() {
        Project draft = new Project();
        draft.setTitle("Private work");
        draft.setSlug("private-work");
        draft.setSummary("summary");
        draft.setDescriptionMarkdown("details");
        draft.setStatus(ArticleStatus.DRAFT);
        draft.setVisibility(ArticleVisibility.PUBLIC);
        given(projectRepository.findBySlug("private-work")).willReturn(Optional.of(draft));

        assertThrows(ProjectNotFoundException.class, () -> projectService.getPublicBySlug("private-work"));
    }

    @Test
    void publicProjectResponseOmitsPrivateLinkedArticleIds() {
        UUID publicArticleId = UUID.randomUUID();
        UUID privateArticleId = UUID.randomUUID();
        Article publicArticle = new Article("Public", "public", null, "content", null,
                ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC, null, false, null, null, null);
        Article privateArticle = new Article("Private", "private", null, "content", null,
                ArticleStatus.PUBLISHED, ArticleVisibility.PRIVATE, null, false, null, null, null);
        ReflectionTestUtils.setField(publicArticle, "id", publicArticleId);
        ReflectionTestUtils.setField(privateArticle, "id", privateArticleId);

        Project project = new Project();
        project.setTitle("Public work");
        project.setSlug("public-work");
        project.setSummary("summary");
        project.setDescriptionMarkdown("details");
        project.setTechStack(List.of());
        project.setGallery(List.of());
        project.setArticles(List.of(publicArticle, privateArticle));
        project.setStatus(ArticleStatus.PUBLISHED);
        project.setVisibility(ArticleVisibility.PUBLIC);
        given(projectRepository.findBySlug("public-work")).willReturn(Optional.of(project));

        ProjectResponse response = projectService.getPublicBySlug("public-work");

        assertEquals(List.of(publicArticleId), response.articleIds());
    }

    @Test
    void createRejectsInvalidProjectDateRange() {
        ProjectRequest request = new ProjectRequest(
                "Work", "work", "summary", "details", List.of(), null, null, null, List.of(),
                null, ArticleStatus.DRAFT, ArticleVisibility.PRIVATE, false, 0,
                java.time.LocalDate.of(2025, 2, 1), java.time.LocalDate.of(2025, 1, 1),
                null, null, List.of());

        assertThrows(IllegalArgumentException.class, () -> projectService.create(request));
    }

    @Test
    void createGeneratesSlugFromChineseTitleWhenSlugIsBlank() {
        given(projectRepository.existsBySlug("项目-标题")).willReturn(false);
        given(projectRepository.save(org.mockito.ArgumentMatchers.any(Project.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        ProjectRequest request = new ProjectRequest(
                "项目 标题", "", "summary", "details", List.of(), null, null, null, List.of(),
                null, ArticleStatus.DRAFT, ArticleVisibility.PRIVATE, false, 0,
                null, null, null, null, List.of());

        ProjectResponse response = projectService.create(request);

        assertEquals("项目-标题", response.slug());
    }
}
