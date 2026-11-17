package com.vsblog.article;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/articles/{articleId}/notes")
@PreAuthorize("hasRole('ADMIN')")
public class ArticleNoteController {

    private final ArticleNoteService noteService;

    public ArticleNoteController(ArticleNoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public ResponseEntity<List<ArticleNoteResponse>> list(@PathVariable UUID articleId) {
        return ResponseEntity.ok(noteService.list(articleId));
    }

    @PostMapping
    public ResponseEntity<ArticleNoteResponse> create(
            @PathVariable UUID articleId,
            @Valid @RequestBody ArticleNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(noteService.create(articleId, request));
    }

    @PutMapping("/{noteId}")
    public ResponseEntity<ArticleNoteResponse> update(
            @PathVariable UUID articleId,
            @PathVariable UUID noteId,
            @Valid @RequestBody ArticleNoteRequest request) {
        return ResponseEntity.ok(noteService.update(articleId, noteId, request));
    }

    @DeleteMapping("/{noteId}")
    public ResponseEntity<Void> delete(@PathVariable UUID articleId, @PathVariable UUID noteId) {
        noteService.delete(articleId, noteId);
        return ResponseEntity.noContent().build();
    }
}
