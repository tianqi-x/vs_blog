package com.vsblog.article;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import com.vsblog.api.ApiExceptionHandler;
import com.vsblog.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TagController.class)
@Import({TagController.class, SecurityConfig.class, ApiExceptionHandler.class})
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @Test
    void tagManagementRequiresAdminAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/tags"))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicTagsEndpointDoesNotRequireAuthentication() throws Exception {
        given(tagService.listPublic()).willReturn(java.util.List.of(
                new TagResponse(UUID.randomUUID(), "Java", "java", null, null)));

        mockMvc.perform(get("/api/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("java"));
    }

    @Test
    void adminCanCreateTagAndReceivesDto() throws Exception {
        given(tagService.create(any(TagRequest.class)))
                .willReturn(new TagResponse(UUID.randomUUID(), "Java", "java", null, null));

        mockMvc.perform(post("/api/admin/tags")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Java\",\"slug\":\"java\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("java"));
    }
}
