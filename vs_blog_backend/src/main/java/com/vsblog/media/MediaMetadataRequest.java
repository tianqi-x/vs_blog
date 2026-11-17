package com.vsblog.media;

import jakarta.validation.constraints.NotBlank;

public record MediaMetadataRequest(
        @NotBlank String alt,
        String caption) {
}
