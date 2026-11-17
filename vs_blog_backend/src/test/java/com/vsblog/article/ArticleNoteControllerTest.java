package com.vsblog.article;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

@WebMvcTest(ArticleNoteController.class)
@Import({ArticleNoteController.class, SecurityConfig.class, ApiExceptionHandler.class})
class ArticleNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ArticleNoteService noteService;

    @Test
    void notesAreNotPublic() throws Exception {
        mockMvc.perform(get("/api/admin/articles/{articleId}/notes", UUID.randomUUID()))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotReadPersonalNotes() throws Exception {
        mockMvc.perform(get("/api/admin/articles/{articleId}/notes", UUID.randomUUID())
                        .with(user("reader").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreatePersonalNote() throws Exception {
        UUID articleId = UUID.randomUUID();
        given(noteService.create(eq(articleId), any(ArticleNoteRequest.class)))
                .willReturn(new ArticleNoteResponse(UUID.randomUUID(), articleId, "takeaway", null, null));

        mockMvc.perform(post("/api/admin/articles/{articleId}/notes", articleId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"takeaway\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.articleId").value(articleId.toString()))
                .andExpect(jsonPath("$.content").value("takeaway"));
    }

    @Test
    void adminCanUpdateAndDeletePersonalNote() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        given(noteService.update(eq(articleId), eq(noteId), any(ArticleNoteRequest.class)))
                .willReturn(new ArticleNoteResponse(noteId, articleId, "updated", null, null));

        mockMvc.perform(put("/api/admin/articles/{articleId}/notes/{noteId}", articleId, noteId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("updated"));

        mockMvc.perform(delete("/api/admin/articles/{articleId}/notes/{noteId}", articleId, noteId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
        verify(noteService).delete(articleId, noteId);
    }
}
