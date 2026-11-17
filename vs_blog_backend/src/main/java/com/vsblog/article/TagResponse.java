package com.vsblog.article;

import java.time.Instant;
import java.util.UUID;

public record TagResponse(UUID id, String name, String slug, Instant createdAt, Instant updatedAt) {
}
