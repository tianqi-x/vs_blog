package com.vsblog.category;

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

import java.util.List;
import java.util.UUID;

import com.vsblog.config.SecurityConfig;
import com.vsblog.api.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CategoryController.class)
@Import({CategoryController.class, SecurityConfig.class, ApiExceptionHandler.class, CategoryControllerTest.TestSecurityConfig.class})
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void listReturnsCategories() throws Exception {
        UUID id = UUID.randomUUID();
        given(categoryService.listAll()).willReturn(List.of(new CategoryResponse(id, "Java", "java", "Java notes", 1, null, null)));

        mockMvc.perform(get("/api/admin/categories").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].slug").value("java"));
    }

    @Test
    void createCategoryStoresValidRequest() throws Exception {
        UUID id = UUID.randomUUID();
        given(categoryService.create(any(CategoryCreateRequest.class)))
                .willReturn(new CategoryResponse(id, "Spring", "spring", "Spring notes", 2, null, null));

        mockMvc.perform(post("/api/admin/categories")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Spring\",\"slug\":\"spring\",\"description\":\"Spring notes\",\"sortOrder\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Spring"))
                .andExpect(jsonPath("$.slug").value("spring"));

        verify(categoryService).create(any(CategoryCreateRequest.class));
    }

    @Test
    void invalidCategoryNameReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/admin/categories")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"slug\":\"empty\",\"sortOrder\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_error"));
    }

    @Test
    void unauthenticatedUserCannotCreateCategory() throws Exception {
        mockMvc.perform(post("/api/admin/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Spring\",\"slug\":\"spring\",\"sortOrder\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCategoryRejectsDuplicateSlug() throws Exception {
        UUID id = UUID.randomUUID();
        given(categoryService.update(eq(id), any(CategoryUpdateRequest.class)))
                .willThrow(new DuplicateSlugException("java"));

        mockMvc.perform(put("/api/admin/categories/{id}", id)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Java\",\"slug\":\"java\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("duplicate_slug"));
    }

    @Test
    void deleteCategoryRemovesEntry() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/categories/{id}", id)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(id);
    }

    static class TestSecurityConfig {
    }
}
