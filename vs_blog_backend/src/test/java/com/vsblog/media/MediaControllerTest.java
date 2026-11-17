package com.vsblog.media;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.UUID;

import com.vsblog.api.ApiExceptionHandler;
import com.vsblog.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(MediaController.class)
@Import({MediaController.class, SecurityConfig.class, ApiExceptionHandler.class})
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    @Test
    void adminCanUploadImageAndReceiveMetadata() throws Exception {
        UUID mediaId = UUID.randomUUID();
        given(mediaService.upload(any(), eq("Alt text"), eq("Caption"))).willReturn(
                new MediaResponse(mediaId, "/api/media/" + mediaId, "cover.png",
                        "Alt text", "Caption", 2, 3, Instant.now()));
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/admin/media")
                        .file(file)
                        .param("alt", "Alt text")
                        .param("caption", "Caption")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(mediaId.toString()))
                .andExpect(jsonPath("$.url").value("/api/media/" + mediaId));
    }

    @Test
    void mediaUploadIsNotAvailableToUnauthenticatedUsers() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "cover.png", "image/png", new byte[]{1});

        mockMvc.perform(multipart("/api/admin/media")
                        .file(file)
                        .param("alt", "Alt text")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void nonAdminCannotListMedia() throws Exception {
        mockMvc.perform(get("/api/admin/media").with(user("reader").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void publicMediaUrlServesImageWithNosniffHeader() throws Exception {
        UUID mediaId = UUID.randomUUID();
        given(mediaService.getPublicFile(mediaId)).willReturn(
                new MediaFileData(new byte[]{1, 2, 3}, "image/png"));

        mockMvc.perform(get("/api/media/{id}", mediaId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes(new byte[]{1, 2, 3}))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void adminCanUpdateAndDeleteMediaMetadata() throws Exception {
        UUID mediaId = UUID.randomUUID();
        given(mediaService.updateMetadata(eq(mediaId), any(MediaMetadataRequest.class))).willReturn(
                new MediaResponse(mediaId, "/api/media/" + mediaId, "cover.png",
                        "Updated alt", null, 2, 3, Instant.now()));

        mockMvc.perform(put("/api/admin/media/{id}", mediaId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"alt\":\"Updated alt\",\"caption\":null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.alt").value("Updated alt"));

        mockMvc.perform(delete("/api/admin/media/{id}", mediaId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(mediaService).delete(mediaId);
    }
}
