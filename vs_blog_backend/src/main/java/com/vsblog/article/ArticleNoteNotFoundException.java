package com.vsblog.article;

import java.util.UUID;

public class ArticleNoteNotFoundException extends RuntimeException {

    public ArticleNoteNotFoundException(UUID id, UUID articleId) {
        super("Note not found for article " + articleId + ": " + id);
    }
}
