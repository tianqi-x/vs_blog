package com.vsblog.project;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.vsblog.article.ArticleStatus;
import com.vsblog.article.ArticleVisibility;

public record ProjectResponse(
        UUID id,
        String title,
        String slug,
        String summary,
        String descriptionMarkdown,
        List<String> techStack,
        String projectUrl,
        String repositoryUrl,
        String coverImageUrl,
        List<String> gallery,
        String role,
        ArticleStatus status,
        ArticleVisibility visibility,
        boolean featured,
        int sortOrder,
        LocalDate startedAt,
        LocalDate completedAt,
        String seoTitle,
        String seoDescription,
        List<UUID> articleIds,
        Instant createdAt,
        Instant updatedAt) {
}
