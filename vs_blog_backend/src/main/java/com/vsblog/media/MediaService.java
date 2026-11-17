package com.vsblog.media;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MediaService {

    private final MediaRepository mediaRepository;
    private final MediaFileStorage mediaFileStorage;

    public MediaService(MediaRepository mediaRepository, MediaFileStorage mediaFileStorage) {
        this.mediaRepository = mediaRepository;
        this.mediaFileStorage = mediaFileStorage;
    }

    @Transactional(readOnly = true)
    public List<MediaResponse> listManaged() {
        return mediaRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public MediaResponse upload(MultipartFile file, String alt, String caption) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file must not be empty");
        }
        if (file.getSize() > MediaFileStorage.MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image file exceeds the 10 MB limit");
        }
        if (alt == null || alt.isBlank()) {
            throw new IllegalArgumentException("Alt text must not be blank");
        }

        String fileName = safeFileName(file.getOriginalFilename());
        byte[] content;
        try {
            content = file.getBytes();
        } catch (IOException exception) {
            throw new MediaStorageException("Unable to read uploaded image", exception);
        }
        ImageMetadata image = ImageMetadata.inspect(content);
        UUID id = UUID.randomUUID();
        Media media = new Media(id, "/api/media/" + id, fileName, alt.trim(),
                caption == null || caption.isBlank() ? null : caption.trim(), image.width(), image.height());

        mediaFileStorage.store(id, content);
        try {
            return toResponse(mediaRepository.save(media));
        } catch (DataAccessException exception) {
            try {
                mediaFileStorage.delete(id);
            } catch (MediaStorageException cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            }
            throw exception;
        }
    }

    @Transactional
    public MediaResponse updateMetadata(UUID id, MediaMetadataRequest request) {
        Media media = findById(id);
        media.setAlt(request.alt().trim());
        media.setCaption(request.caption() == null || request.caption().isBlank()
                ? null
                : request.caption().trim());
        return toResponse(mediaRepository.save(media));
    }

    @Transactional(readOnly = true)
    public MediaFileData getPublicFile(UUID id) {
        findById(id);
        return mediaFileStorage.load(id);
    }

    @Transactional
    public void delete(UUID id) {
        Media media = findById(id);
        mediaRepository.delete(media);
        mediaFileStorage.delete(id);
    }

    private Media findById(UUID id) {
        return mediaRepository.findById(id).orElseThrow(() -> new MediaNotFoundException(id));
    }

    private String safeFileName(String originalFileName) {
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Image file name is required");
        }
        String name = originalFileName.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).trim();
        if (name.isEmpty() || name.length() > 255
                || name.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Image file name must be between 1 and 255 characters");
        }
        return name;
    }

    private MediaResponse toResponse(Media media) {
        return new MediaResponse(media.getId(), media.getUrl(), media.getFileName(), media.getAlt(),
                media.getCaption(), media.getWidth(), media.getHeight(), media.getCreatedAt());
    }
}
