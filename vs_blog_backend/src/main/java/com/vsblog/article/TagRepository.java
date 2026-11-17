package com.vsblog.article;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findAllByOrderByNameAsc();

    @Query("""
            select distinct t
            from Article a join a.tags t
            where a.status = :status and a.visibility = :visibility
            order by t.name
            """)
    List<Tag> findPublicTags(
            @Param("status") ArticleStatus status,
            @Param("visibility") ArticleVisibility visibility);

    Set<Tag> findAllByIdIn(Iterable<UUID> ids);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
