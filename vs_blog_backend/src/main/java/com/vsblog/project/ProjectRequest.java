package com.vsblog.project;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.vsblog.article.ArticleStatus;
import com.vsblog.article.ArticleVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProjectRequest(
        @NotBlank(message = "Project title is required")
        @Size(max = 255, message = "Title must be at most 255 characters")
        String title,
        String slug,
        @NotBlank(message = "Project summary is required")
        String summary,
        @NotBlank(message = "Project description is required")
        String descriptionMarkdown,
        List<String> techStack,
        String projectUrl,
        String repositoryUrl,
        String coverImageUrl,
        List<String> gallery,
        @Size(max = 255, message = "Role must be at most 255 characters")
        String role,
        ArticleStatus status,
        ArticleVisibility visibility,
        Boolean featured,
        Integer sortOrder,
        LocalDate startedAt,
        LocalDate completedAt,
        String seoTitle,
        String seoDescription,
        List<UUID> articleIds) {
}
