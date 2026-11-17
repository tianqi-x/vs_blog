package com.vsblog.project;

import java.util.UUID;

public class ProjectNotFoundException extends RuntimeException {

    public ProjectNotFoundException(UUID id) { super("Project not found: " + id); }
    public ProjectNotFoundException(String slug) { super("Project not found: " + slug); }
}
