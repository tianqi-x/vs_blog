package com.vsblog.project;

import java.util.Optional;
import java.util.UUID;

import com.vsblog.article.ArticleStatus;
import com.vsblog.article.ArticleVisibility;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, UUID> {

    @EntityGraph(attributePaths = "articles")
    Optional<Project> findBySlug(String slug);

    @EntityGraph(attributePaths = "articles")
    java.util.List<Project> findByStatusAndVisibility(ArticleStatus status, ArticleVisibility visibility, Sort sort);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    @Override
    @EntityGraph(attributePaths = "articles")
    Optional<Project> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = "articles")
    java.util.List<Project> findAll(Sort sort);
}
