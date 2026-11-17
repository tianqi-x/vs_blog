package com.vsblog.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.time.Instant;

import com.vsblog.category.CategoryRepository;
import com.vsblog.category.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private ArticleService articleService;

    private Article existingArticle;

    @BeforeEach
    void setUp() {
        existingArticle = new Article(
                "Original title", "stable-slug", "Summary", "Markdown", null,
                ArticleStatus.DRAFT, ArticleVisibility.PRIVATE, null, true, null, null, null);
    }

    @Test
    void createDefaultsToDraftAndPrivateWithoutPublicationTime() {
        given(articleRepository.existsBySlug("new-note")).willReturn(false);
        given(articleRepository.save(any(Article.class))).willAnswer(invocation -> invocation.getArgument(0));

        ArticleResponse response = articleService.create(request("New note", null, null, null));

        assertEquals(ArticleStatus.DRAFT, response.status());
        assertEquals(ArticleVisibility.PRIVATE, response.visibility());
        assertNull(response.publishedAt());
        assertEquals("new-note", response.slug());
    }

    @Test
    void createGeneratesSlugFromChineseTitleWhenSlugIsBlank() {
        given(articleRepository.existsBySlug("中文-标题")).willReturn(false);
        given(articleRepository.save(any(Article.class))).willAnswer(invocation -> invocation.getArgument(0));

        ArticleResponse response = articleService.create(request("中文 标题", "", null, null));

        assertEquals("中文-标题", response.slug());
    }

    @Test
    void publishedPrivateArticleReceivesPublicationTime() {
        given(articleRepository.existsBySlug("private-post")).willReturn(false);
        given(articleRepository.save(any(Article.class))).willAnswer(invocation -> invocation.getArgument(0));

        ArticleResponse response = articleService.create(
                request("Private post", "private-post", ArticleStatus.PUBLISHED, ArticleVisibility.PRIVATE));

        assertNotNull(response.publishedAt());
    }

    @Test
    void duplicateSlugOnCreateIsRejectedBeforeSaving() {
        given(articleRepository.existsBySlug("stable-slug")).willReturn(true);

        assertThrows(DuplicateArticleSlugException.class,
                () -> articleService.create(request("A title", "stable-slug", ArticleStatus.DRAFT, ArticleVisibility.PRIVATE)));

        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void createAssociatesExistingTagsWithArticle() {
        UUID tagId = UUID.randomUUID();
        Tag tag = new Tag("Java", "java");
        ReflectionTestUtils.setField(tag, "id", tagId);
        given(articleRepository.existsBySlug("tagged-note")).willReturn(false);
        given(tagRepository.findAllByIdIn(List.of(tagId))).willReturn(Set.of(tag));
        given(articleRepository.save(any(Article.class))).willAnswer(invocation -> invocation.getArgument(0));

        ArticleResponse response = articleService.create(
                new ArticleRequest("Tagged note", "tagged-note", "Summary", "Markdown", null,
                        ArticleStatus.DRAFT, ArticleVisibility.PRIVATE, null, null, null, null, List.of(tagId)));

        assertEquals(List.of(tagId), response.tagIds());
    }

    @Test
    void createAssociatesOneExistingCategory() {
        UUID categoryId = UUID.randomUUID();
        Category category = new Category("Java", "java", null, 0);
        ReflectionTestUtils.setField(category, "id", categoryId);
        given(categoryRepository.findById(categoryId)).willReturn(Optional.of(category));
        given(articleRepository.existsBySlug("categorized-note")).willReturn(false);
        given(articleRepository.save(any(Article.class))).willAnswer(invocation -> invocation.getArgument(0));

        ArticleResponse response = articleService.create(
                new ArticleRequest("Categorized note", "categorized-note", "Summary", "Markdown", categoryId,
                        ArticleStatus.DRAFT, ArticleVisibility.PRIVATE, null, null, null, null, List.of()));

        assertEquals(categoryId, response.categoryId());
    }

    @Test
    void createRejectsUnknownTagIds() {
        UUID missingTagId = UUID.randomUUID();
        given(articleRepository.existsBySlug("tagged-note")).willReturn(false);
        given(tagRepository.findAllByIdIn(List.of(missingTagId))).willReturn(Set.of());

        assertThrows(IllegalArgumentException.class, () -> articleService.create(
                new ArticleRequest("Tagged note", "tagged-note", "Summary", "Markdown", null,
                        ArticleStatus.DRAFT, ArticleVisibility.PRIVATE, null, null, null, null, List.of(missingTagId))));
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void titleChangeWithoutSlugKeepsExistingSlug() {
        UUID id = UUID.randomUUID();
        given(articleRepository.findById(id)).willReturn(Optional.of(existingArticle));
        given(articleRepository.existsBySlugAndIdNot("stable-slug", id)).willReturn(false);
        given(articleRepository.save(existingArticle)).willReturn(existingArticle);

        ArticleResponse response = articleService.update(
                id,
                new ArticleRequest("Renamed article", null, "Summary", "Updated markdown", null,
                        null, null, null, null, null, null, null));

        assertEquals("Renamed article", response.title());
        assertEquals("stable-slug", response.slug());
        assertEquals(ArticleStatus.DRAFT, response.status());
        assertEquals(ArticleVisibility.PRIVATE, response.visibility());
        assertTrue(response.pinned());
    }

    @Test
    void duplicateSlugOnUpdateIsRejected() {
        UUID id = UUID.randomUUID();
        given(articleRepository.findById(id)).willReturn(Optional.of(existingArticle));
        given(articleRepository.existsBySlugAndIdNot("taken-slug", id)).willReturn(true);

        assertThrows(DuplicateArticleSlugException.class,
                () -> articleService.update(id, request("Changed", "taken-slug", ArticleStatus.DRAFT, ArticleVisibility.PRIVATE)));

        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void publicLookupHidesDraftPrivateAndArchivedArticles() {
        for (ArticleStatus status : ArticleStatus.values()) {
            for (ArticleVisibility visibility : ArticleVisibility.values()) {
                if (status == ArticleStatus.PUBLISHED && visibility == ArticleVisibility.PUBLIC) {
                    continue;
                }
                Article hidden = new Article("Hidden", "hidden", null, "text", null, status, visibility,
                        null, false, null, null, null);
                given(articleRepository.findBySlug("hidden")).willReturn(Optional.of(hidden));

                assertThrows(ArticleNotFoundException.class, () -> articleService.getPublicBySlug("hidden"));
            }
        }
    }

    @Test
    void publicLookupReturnsPublishedPublicArticle() {
        Article publicArticle = new Article("Public", "public", "Summary", "Markdown", null,
                ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC, null, false, null, null, Instant.now());
        given(articleRepository.findBySlug("public")).willReturn(Optional.of(publicArticle));

        ArticleResponse response = articleService.getPublicBySlug("public");

        assertEquals("public", response.slug());
        assertEquals(ArticleStatus.PUBLISHED, response.status());
        assertEquals(ArticleVisibility.PUBLIC, response.visibility());
    }

    @Test
    void publicListQueriesOnlyPublishedPublicArticles() {
        given(articleRepository.findPublishedPublicOrdered(ArticleVisibility.PUBLIC, ArticleStatus.PUBLISHED))
                .willReturn(java.util.List.of());

        articleService.listPublishedPublic();

        verify(articleRepository).findPublishedPublicOrdered(ArticleVisibility.PUBLIC, ArticleStatus.PUBLISHED);
        verify(articleRepository, never()).findPublishedPublicOrdered(
                eq(ArticleVisibility.PRIVATE), any(ArticleStatus.class));
    }

    @Test
    void searchTrimsQueryAndReturnsPagedPublicArticleResponses() {
        Article publicArticle = new Article("Spring Guide", "spring-guide", "Summary", "Spring content", null,
                ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC, null, false, null, null, Instant.now());
        Page<Article> matchingArticles = new PageImpl<>(List.of(publicArticle), PageRequest.of(1, 5), 6);
        given(articleRepository.searchPublicByText("Spring", PageRequest.of(1, 5)))
                .willReturn(matchingArticles);

        Page<ArticleResponse> response = articleService.searchPublic("  Spring  ", 1, 5);

        assertEquals(6, response.getTotalElements());
        assertEquals("spring-guide", response.getContent().get(0).slug());
        verify(articleRepository).searchPublicByText("Spring", PageRequest.of(1, 5));
    }

    @Test
    void searchRejectsBlankQuery() {
        assertThrows(IllegalArgumentException.class, () -> articleService.searchPublic("  ", 0, 10));

        verify(articleRepository, never()).searchPublicByText(any(), any());
    }

    @Test
    void draftCanBePublishedAndGetsPublicationTime() {
        UUID id = UUID.randomUUID();
        given(articleRepository.findById(id)).willReturn(Optional.of(existingArticle));
        given(articleRepository.existsBySlugAndIdNot("stable-slug", id)).willReturn(false);
        given(articleRepository.save(existingArticle)).willReturn(existingArticle);

        ArticleResponse response = articleService.update(
                id,
                new ArticleRequest("Original title", null, "Summary", "Markdown", null,
                        ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC, null, null, null, null, null));

        assertNotNull(response.publishedAt());
    }

    private ArticleRequest request(String title, String slug, ArticleStatus status, ArticleVisibility visibility) {
        return new ArticleRequest(title, slug, "Summary", "Markdown", null, status, visibility,
                null, null, null, null, null);
    }
}
