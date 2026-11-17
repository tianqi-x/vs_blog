package com.vsblog.article;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleNoteRepository extends JpaRepository<ArticleNote, UUID> {

    List<ArticleNote> findAllByArticleIdOrderByCreatedAtDesc(UUID articleId);

    Optional<ArticleNote> findByIdAndArticleId(UUID id, UUID articleId);
}
