package com.vsblog.project;

public class DuplicateProjectSlugException extends RuntimeException {

    public DuplicateProjectSlugException(String slug) {
        super("Project slug already exists: " + slug);
    }
}
