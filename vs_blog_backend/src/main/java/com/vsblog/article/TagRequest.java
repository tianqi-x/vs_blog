package com.vsblog.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TagRequest(
        @NotBlank(message = "Tag name is required")
        @Size(max = 120, message = "Tag name must be at most 120 characters")
        String name,
        String slug) {
}
