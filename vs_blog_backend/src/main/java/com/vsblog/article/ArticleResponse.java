package com.vsblog.article;

import java.time.Instant;
import java.util.UUID;
import java.util.List;

public record ArticleResponse(
        UUID id,
        String title,
        String slug,
        String summary,
        String contentMarkdown,
        UUID categoryId,
        ArticleStatus status,
        ArticleVisibility visibility,
        String coverImageUrl,
        boolean pinned,
        String seoTitle,
        String seoDescription,
        Instant publishedAt,
        Instant createdAt,
        Instant updatedAt,
        List<UUID> tagIds) {

    public ArticleResponse(UUID id, String title, String slug, String summary, String contentMarkdown,
                           UUID categoryId, ArticleStatus status, ArticleVisibility visibility, String coverImageUrl,
                           boolean pinned, String seoTitle, String seoDescription, Instant publishedAt,
                           Instant createdAt, Instant updatedAt) {
        this(id, title, slug, summary, contentMarkdown, categoryId, status, visibility, coverImageUrl, pinned,
                seoTitle, seoDescription, publishedAt, createdAt, updatedAt, List.of());
    }
}
