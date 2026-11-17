package com.vsblog.media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MediaFileStorage {

    static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final Path storageRoot;

    public MediaFileStorage(@Value("${app.media.storage-dir:./var/media}") String storageDirectory) {
        storageRoot = Path.of(storageDirectory).toAbsolutePath().normalize();
    }

    public void store(UUID id, byte[] content) {
        if (content.length > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image file exceeds the 10 MB limit");
        }
        try {
            Files.createDirectories(storageRoot);
            Files.write(filePath(id), content, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new MediaStorageException("Unable to store media file", exception);
        }
    }

    public MediaFileData load(UUID id) {
        Path path = filePath(id);
        try {
            long size = Files.size(path);
            if (size > MAX_FILE_SIZE) {
                throw new MediaStorageException("Stored media file exceeds the supported size", null);
            }
            byte[] content = Files.readAllBytes(path);
            return new MediaFileData(content, ImageMetadata.detectContentType(content));
        } catch (IOException exception) {
            throw new MediaStorageException("Unable to read media file", exception);
        } catch (IllegalArgumentException exception) {
            throw new MediaStorageException("Stored media file is invalid", exception);
        }
    }

    public void delete(UUID id) {
        try {
            Files.deleteIfExists(filePath(id));
        } catch (IOException exception) {
            throw new MediaStorageException("Unable to delete media file", exception);
        }
    }

    private Path filePath(UUID id) {
        Path path = storageRoot.resolve(id.toString()).normalize();
        if (!path.startsWith(storageRoot)) {
            throw new IllegalArgumentException("Invalid media identifier");
        }
        return path;
    }
}
