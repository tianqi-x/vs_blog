package com.vsblog.category;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        String description,
        Integer sortOrder,
        Instant createdAt,
        Instant updatedAt) {
}
