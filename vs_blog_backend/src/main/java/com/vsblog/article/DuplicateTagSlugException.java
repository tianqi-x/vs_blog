package com.vsblog.article;

public class DuplicateTagSlugException extends RuntimeException {

    public DuplicateTagSlugException(String slug) {
        super("Tag slug already exists: " + slug);
    }
}
