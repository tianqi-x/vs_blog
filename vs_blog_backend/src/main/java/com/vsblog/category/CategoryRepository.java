package com.vsblog.category;

import java.util.UUID;

import com.vsblog.article.ArticleStatus;
import com.vsblog.article.ArticleVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    @Query("select count(c) > 0 from Category c where c.slug = :slug")
    boolean existsBySlug(String slug);

    @Query("select count(c) > 0 from Category c where c.slug = :slug and c.id <> :id")
    boolean existsBySlugAndIdNot(String slug, UUID id);

    @Query("""
            select distinct c
            from Article a join a.category c
            where a.status = :status and a.visibility = :visibility
            order by c.sortOrder, c.name
            """)
    java.util.List<Category> findPublicCategories(
            @Param("status") ArticleStatus status,
            @Param("visibility") ArticleVisibility visibility);
}