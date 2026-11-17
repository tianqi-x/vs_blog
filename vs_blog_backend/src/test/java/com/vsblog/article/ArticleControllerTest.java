package com.vsblog.article;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.vsblog.api.ApiExceptionHandler;
import com.vsblog.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ArticleController.class)
@Import({ArticleController.class, SecurityConfig.class, ApiExceptionHandler.class})
class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleService articleService;

    @Test
    void publicArticlesEndpointReturnsOnlyPublishedPublicItems() throws Exception {
        UUID categoryId = UUID.randomUUID();
        given(articleService.listPublishedPublic()).willReturn(List.of(
                new ArticleResponse(UUID.randomUUID(), "Java Guide", "java-guide", "Summary", "# Hello", categoryId,
                        ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC, null, false, null, null, Instant.now(), Instant.now(), Instant.now())));

        mockMvc.perform(get("/api/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Guide"))
                .andExpect(jsonPath("$[0].visibility").value("PUBLIC"));
    }

    @Test
    void publicSearchEndpointUsesPaginationDefaults() throws Exception {
        ArticleResponse article = new ArticleResponse(UUID.randomUUID(), "Spring Guide", "spring-guide", "Summary",
                "# Spring", null, ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC, null, false,
                null, null, Instant.now(), Instant.now(), Instant.now());
        Page<ArticleResponse> results = new PageImpl<>(List.of(article), PageRequest.of(0, 10), 1);
        given(articleService.searchPublic("spring", 0, 10)).willReturn(results);

        mockMvc.perform(get("/api/search").param("q", "spring"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].slug").value("spring-guide"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(articleService).searchPublic("spring", 0, 10);
    }

    @Test
    void publicSearchRequiresQueryAndReturnsStableValidationError() throws Exception {
        mockMvc.perform(get("/api/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"));
    }

    @Test
    void publicArticleLookupBySlugReturnsPublishedRecord() throws Exception {
        UUID categoryId = UUID.randomUUID();
        given(articleService.getPublicBySlug("java-guide")).willReturn(
                new ArticleResponse(UUID.randomUUID(), "Java Guide", "java-guide", "Summary", "# Hello", categoryId,
                        ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC, null, false, null, null, Instant.now(), Instant.now(), Instant.now()));

        mockMvc.perform(get("/api/articles/java-guide"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("java-guide"));
    }

    @Test
    void adminListRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/articles"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreateArticleRequiresAdminRole() throws Exception {
        mockMvc.perform(post("/api/admin/articles")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\",\"slug\":\"test\",\"contentMarkdown\":\"# hi\",\"status\":\"DRAFT\",\"visibility\":\"PRIVATE\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminUserCannotManageArticles() throws Exception {
        mockMvc.perform(get("/api/admin/articles").with(user("reader").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCreateArticleAllowsAdminUser() throws Exception {
        UUID articleId = UUID.randomUUID();
        given(articleService.create(any(ArticleRequest.class)))
                .willReturn(new ArticleResponse(articleId, "Test", "test", "Summary", "# hi", null,
                        ArticleStatus.DRAFT, ArticleVisibility.PRIVATE, null, false, null, null, null, Instant.now(), Instant.now()));

        mockMvc.perform(post("/api/admin/articles")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\",\"slug\":\"test\",\"contentMarkdown\":\"# hi\",\"status\":\"DRAFT\",\"visibility\":\"PRIVATE\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("test"));

        verify(articleService).create(any(ArticleRequest.class));
    }

    @Test
    void duplicateArticleSlugReturnsConflictResponse() throws Exception {
        given(articleService.create(any(ArticleRequest.class)))
                .willThrow(new DuplicateArticleSlugException("test"));

        mockMvc.perform(post("/api/admin/articles")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\",\"slug\":\"test\",\"contentMarkdown\":\"# hi\",\"status\":\"DRAFT\",\"visibility\":\"PRIVATE\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("duplicate_slug"));
    }

    @Test
    void adminUpdateArticleAllowsAdminUser() throws Exception {
        UUID articleId = UUID.randomUUID();
        given(articleService.update(eq(articleId), any(ArticleRequest.class)))
                .willReturn(new ArticleResponse(articleId, "Updated", "updated", "Summary", "# hi", null,
                        ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC, null, false, null, null, Instant.now(), Instant.now(), Instant.now()));

        mockMvc.perform(put("/api/admin/articles/{id}", articleId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"slug\":\"updated\",\"contentMarkdown\":\"# hi\",\"status\":\"PUBLISHED\",\"visibility\":\"PUBLIC\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("updated"));
    }

    @Test
    void adminDeleteArticleAllowsAdminUser() throws Exception {
        UUID articleId = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/articles/{id}", articleId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(articleService).delete(articleId);
    }
}
