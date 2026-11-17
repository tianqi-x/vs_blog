package com.vsblog.article;

import jakarta.validation.constraints.NotBlank;

public record ArticleNoteRequest(
        @NotBlank(message = "Note content is required")
        String content) {
}
