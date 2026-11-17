package com.vsblog.media;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "blog_media", schema = "public")
public class Media {

    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String url;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String alt;

    @Column(columnDefinition = "TEXT")
    private String caption;

    private Integer width;

    private Integer height;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private Instant createdAt;

    protected Media() {
    }

    public Media(UUID id, String url, String fileName, String alt, String caption, Integer width, Integer height) {
        this.id = id;
        this.url = url;
        this.fileName = fileName;
        this.alt = alt;
        this.caption = caption;
        this.width = width;
        this.height = height;
    }

    public UUID getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public Integer getWidth() {
        return width;
    }

    public Integer getHeight() {
        return height;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
