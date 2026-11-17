package com.vsblog.article;

import java.util.UUID;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArticleRequest(
        @NotBlank(message = "Article title is required")
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,
        String slug,
        @Size(max = 1000, message = "Summary must be at most 1000 characters")
        String summary,
        @NotBlank(message = "Content is required")
        String contentMarkdown,
        UUID categoryId,
        ArticleStatus status,
        ArticleVisibility visibility,
        String coverImageUrl,
        Boolean pinned,
        String seoTitle,
        @Size(max = 1000, message = "SEO description must be at most 1000 characters")
        String seoDescription,
        List<UUID> tagIds) {

    public ArticleRequest(String title, String slug, String summary, String contentMarkdown, UUID categoryId,
                          ArticleStatus status, ArticleVisibility visibility, String coverImageUrl, Boolean pinned,
                          String seoTitle, String seoDescription) {
        this(title, slug, summary, contentMarkdown, categoryId, status, visibility, coverImageUrl, pinned,
                seoTitle, seoDescription, null);
    }
}
