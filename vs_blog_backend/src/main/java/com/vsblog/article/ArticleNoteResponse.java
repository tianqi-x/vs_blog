package com.vsblog.article;

import java.time.Instant;
import java.util.UUID;

public record ArticleNoteResponse(UUID id, UUID articleId, String content, Instant createdAt, Instant updatedAt) {
}
