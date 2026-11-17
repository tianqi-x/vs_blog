package com.vsblog.media;

import java.time.Instant;
import java.util.UUID;

public record MediaResponse(
        UUID id,
        String url,
        String fileName,
        String alt,
        String caption,
        Integer width,
        Integer height,
        Instant createdAt) {
}
