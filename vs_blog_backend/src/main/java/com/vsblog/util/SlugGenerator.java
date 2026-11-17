package com.vsblog.util;

import java.util.Locale;
import java.util.UUID;

public final class SlugGenerator {

    private SlugGenerator() {
    }

    public static String generate(String value, String fallback) {
        boolean useFallback = value == null || value.isBlank();
        String candidate = useFallback ? fallback : value;
        String slug = candidate.strip().toLowerCase(Locale.ROOT)
                .replaceAll("(?U)[^\\p{L}\\p{N}\\s-]", "")
                .strip()
                .replaceAll("(?U)\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
        if (slug.isEmpty()) {
            if (useFallback) {
                return UUID.randomUUID().toString();
            }
            throw new IllegalArgumentException("Slug must not be empty");
        }
        return slug;
    }
}
