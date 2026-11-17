package com.vsblog.project;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.vsblog.article.ArticleStatus;
import com.vsblog.article.ArticleVisibility;
import com.vsblog.api.ApiExceptionHandler;
import com.vsblog.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProjectController.class)
@Import({ProjectController.class, SecurityConfig.class, ApiExceptionHandler.class})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProjectService projectService;

    @Test
    void publicProjectsEndpointReturnsPublicProjectDto() throws Exception {
        given(projectService.listPublic()).willReturn(List.of(new ProjectResponse(
                UUID.randomUUID(), "Portfolio", "portfolio", "Summary", "Details", List.of("Java"),
                null, null, null, List.of(), "Developer", ArticleStatus.PUBLISHED,
                ArticleVisibility.PUBLIC, true, 1, null, null, null, null, List.of(), null, null)));

        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("portfolio"))
                .andExpect(jsonPath("$[0].techStack[0]").value("Java"));
    }

    @Test
    void projectManagementRequiresAdminAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/projects"))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadManagedProjects() throws Exception {
        given(projectService.listManaged()).willReturn(List.of());

        mockMvc.perform(get("/api/admin/projects").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void adminCanCreateProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        given(projectService.create(any(ProjectRequest.class))).willReturn(new ProjectResponse(
                projectId, "Portfolio", "portfolio", "Summary", "Details", List.of("Java"),
                null, null, null, List.of(), "Developer", ArticleStatus.DRAFT,
                ArticleVisibility.PRIVATE, false, 0, null, null, null, null, List.of(), null, null));

        mockMvc.perform(post("/api/admin/projects")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Portfolio\",\"slug\":\"portfolio\",\"summary\":\"Summary\",\"descriptionMarkdown\":\"Details\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("portfolio"));
    }

    @Test
    void adminCanUpdateAndDeleteProject() throws Exception {
        UUID projectId = UUID.randomUUID();
        given(projectService.update(eq(projectId), any(ProjectRequest.class))).willReturn(new ProjectResponse(
                projectId, "Updated", "updated", "Summary", "Details", List.of(), null, null, null, List.of(),
                null, ArticleStatus.DRAFT, ArticleVisibility.PRIVATE, false, 0, null, null, null, null,
                List.of(), null, null));

        mockMvc.perform(put("/api/admin/projects/{id}", projectId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated\",\"slug\":\"updated\",\"summary\":\"Summary\",\"descriptionMarkdown\":\"Details\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("updated"));
        verify(projectService).update(eq(projectId), any(ProjectRequest.class));

        mockMvc.perform(delete("/api/admin/projects/{id}", projectId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
        verify(projectService).delete(projectId);
    }
}
