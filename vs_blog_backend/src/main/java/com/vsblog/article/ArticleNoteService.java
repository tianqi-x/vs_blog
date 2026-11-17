package com.vsblog.article;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleNoteService {

    private final ArticleNoteRepository noteRepository;
    private final ArticleRepository articleRepository;

    public ArticleNoteService(ArticleNoteRepository noteRepository, ArticleRepository articleRepository) {
        this.noteRepository = noteRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public List<ArticleNoteResponse> list(UUID articleId) {
        ensureArticleExists(articleId);
        return noteRepository.findAllByArticleIdOrderByCreatedAtDesc(articleId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public ArticleNoteResponse create(UUID articleId, ArticleNoteRequest request) {
        ensureArticleExists(articleId);
        return toResponse(noteRepository.save(new ArticleNote(articleId, request.content())));
    }

    @Transactional
    public ArticleNoteResponse update(UUID articleId, UUID noteId, ArticleNoteRequest request) {
        ArticleNote note = findNote(articleId, noteId);
        note.setContent(request.content());
        return toResponse(noteRepository.save(note));
    }

    @Transactional
    public void delete(UUID articleId, UUID noteId) {
        ArticleNote note = findNote(articleId, noteId);
        noteRepository.delete(note);
    }

    private void ensureArticleExists(UUID articleId) {
        if (!articleRepository.existsById(articleId)) {
            throw new ArticleNotFoundException(articleId);
        }
    }

    private ArticleNote findNote(UUID articleId, UUID noteId) {
        ensureArticleExists(articleId);
        return noteRepository.findByIdAndArticleId(noteId, articleId)
                .orElseThrow(() -> new ArticleNoteNotFoundException(noteId, articleId));
    }

    private ArticleNoteResponse toResponse(ArticleNote note) {
        return new ArticleNoteResponse(
                note.getId(),
                note.getArticleId(),
                note.getContent(),
                note.getCreatedAt(),
                note.getUpdatedAt());
    }
}
