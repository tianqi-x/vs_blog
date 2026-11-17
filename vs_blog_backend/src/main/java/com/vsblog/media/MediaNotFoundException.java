package com.vsblog.media;

import java.util.UUID;

public class MediaNotFoundException extends RuntimeException {

    public MediaNotFoundException(UUID id) {
        super("Media not found: " + id);
    }
}
