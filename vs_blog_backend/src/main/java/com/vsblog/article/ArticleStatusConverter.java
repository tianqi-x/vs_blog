package com.vsblog.article;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ArticleStatusConverter implements AttributeConverter<ArticleStatus, String> {

    @Override
    public String convertToDatabaseColumn(ArticleStatus status) {
        return status == null ? null : status.name().toLowerCase(java.util.Locale.ROOT);
    }

    @Override
    public ArticleStatus convertToEntityAttribute(String value) {
        return value == null ? null : ArticleStatus.valueOf(value.toUpperCase(java.util.Locale.ROOT));
    }
}
