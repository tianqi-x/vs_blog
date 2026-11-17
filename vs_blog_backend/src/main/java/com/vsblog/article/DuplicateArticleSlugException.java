package com.vsblog.article;

public class DuplicateArticleSlugException extends RuntimeException {

    public DuplicateArticleSlugException(String slug) {
        super("Article slug already exists: " + slug);
    }
}
