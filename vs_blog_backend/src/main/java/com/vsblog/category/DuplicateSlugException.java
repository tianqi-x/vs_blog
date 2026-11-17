package com.vsblog.category;

public class DuplicateSlugException extends RuntimeException {

    public DuplicateSlugException(String slug) {
        super("Category slug already exists: " + slug);
    }
}
