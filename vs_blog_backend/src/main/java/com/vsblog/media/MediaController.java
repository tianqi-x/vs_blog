package com.vsblog.media;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class MediaController {

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @GetMapping("/admin/media")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MediaResponse>> listMedia() {
        return ResponseEntity.ok(mediaService.listManaged());
    }

    @PostMapping(value = "/admin/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MediaResponse> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("alt") String alt,
            @RequestParam(value = "caption", required = false) String caption) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.upload(file, alt, caption));
    }

    @PutMapping("/admin/media/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MediaResponse> updateMediaMetadata(
            @PathVariable UUID id,
            @Valid @RequestBody MediaMetadataRequest request) {
        return ResponseEntity.ok(mediaService.updateMetadata(id, request));
    }

    @DeleteMapping("/admin/media/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedia(@PathVariable UUID id) {
        mediaService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/media/{id}")
    public ResponseEntity<byte[]> mediaFile(@PathVariable UUID id) {
        MediaFileData media = mediaService.getPublicFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(media.contentType()))
                .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(1)).cachePublic())
                .header("X-Content-Type-Options", "nosniff")
                .body(media.content());
    }
}
