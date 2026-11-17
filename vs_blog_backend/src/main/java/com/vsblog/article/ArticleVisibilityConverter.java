package com.vsblog.article;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ArticleVisibilityConverter implements AttributeConverter<ArticleVisibility, String> {

    @Override
    public String convertToDatabaseColumn(ArticleVisibility visibility) {
        return visibility == null ? null : visibility.name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public ArticleVisibility convertToEntityAttribute(String value) {
        return value == null ? null : ArticleVisibility.valueOf(value.toUpperCase(java.util.Locale.ROOT));
    }
}
