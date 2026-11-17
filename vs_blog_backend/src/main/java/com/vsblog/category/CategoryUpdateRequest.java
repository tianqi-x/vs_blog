package com.vsblog.category;

import jakarta.validation.constraints.Size;

public record CategoryUpdateRequest(
        @Size(max = 120, message = "Category name must be at most 120 characters")
        String name,
        String slug,
        @Size(max = 1000, message = "Description must be at most 1000 characters")
        String description,
        Integer sortOrder) {
}
