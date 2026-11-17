package com.vsblog.article;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, UUID> {

    Optional<Article> findBySlug(String slug);

    @EntityGraph(attributePaths = {"category", "tags"})
    @Query("select a from Article a where a.visibility = :visibility and a.status = :status order by a.publishedAt desc nulls last")
    List<Article> findPublishedPublicOrdered(ArticleVisibility visibility, ArticleStatus status);

    @Query("select a from Article a where a.visibility = :visibility and a.status = :status")
    Page<Article> findPublishedPublicPage(ArticleVisibility visibility, ArticleStatus status, Pageable pageable);

    @Query(value = """
            select a.*
            from public.blog_articles a
            where a.status = 'published'
              and a.visibility = 'public'
              and (
                  strpos(lower(a.title), lower(:query)) > 0
                  or strpos(lower(a.content_markdown), lower(:query)) > 0
                  or exists (
                      select 1
                      from public.blog_tags_articles ta
                      join public.blog_tags t on t.id = ta.tag_id
                      where ta.article_id = a.id
                        and (strpos(lower(t.name), lower(:query)) > 0
                             or strpos(lower(t.slug), lower(:query)) > 0)
                  )
              )
            order by a.published_at desc nulls last, a.id
            """,
            countQuery = """
                    select count(*)
                    from public.blog_articles a
                    where a.status = 'published'
                      and a.visibility = 'public'
                      and (
                          strpos(lower(a.title), lower(:query)) > 0
                          or strpos(lower(a.content_markdown), lower(:query)) > 0
                          or exists (
                              select 1
                              from public.blog_tags_articles ta
                              join public.blog_tags t on t.id = ta.tag_id
                              where ta.article_id = a.id
                                and (strpos(lower(t.name), lower(:query)) > 0
                                     or strpos(lower(t.slug), lower(:query)) > 0)
                          )
                      )
                    """,
            nativeQuery = true)
    Page<Article> searchPublicByText(@Param("query") String query, Pageable pageable);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    @Override
    @EntityGraph(attributePaths = {"category", "tags"})
    java.util.Optional<Article> findById(UUID id);

    @Override
    @EntityGraph(attributePaths = {"category", "tags"})
    java.util.List<Article> findAll(org.springframework.data.domain.Sort sort);
}
