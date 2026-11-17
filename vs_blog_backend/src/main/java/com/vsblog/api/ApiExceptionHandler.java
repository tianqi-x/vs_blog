package com.vsblog.api;

import java.util.Map;

import com.vsblog.article.ArticleNotFoundException;
import com.vsblog.article.ArticleNoteNotFoundException;
import com.vsblog.article.DuplicateArticleSlugException;
import com.vsblog.article.DuplicateTagSlugException;
import com.vsblog.article.TagNotFoundException;
import com.vsblog.project.DuplicateProjectSlugException;
import com.vsblog.project.ProjectNotFoundException;
import com.vsblog.category.CategoryNotFoundException;
import com.vsblog.category.DuplicateSlugException;
import com.vsblog.media.MediaNotFoundException;
import com.vsblog.media.MediaStorageException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryNotFound(CategoryNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "not_found", "message", ex.getMessage()));
    }

    @ExceptionHandler(DuplicateSlugException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateSlug(DuplicateSlugException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "duplicate_slug", "message", ex.getMessage()));
    }

    @ExceptionHandler({DuplicateArticleSlugException.class, DuplicateTagSlugException.class,
            DuplicateProjectSlugException.class})
    public ResponseEntity<Map<String, Object>> handleContentSlugConflict(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "duplicate_slug", "message", ex.getMessage()));
    }

    @ExceptionHandler({ArticleNotFoundException.class, ArticleNoteNotFoundException.class,
            TagNotFoundException.class, ProjectNotFoundException.class, MediaNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleContentNotFound(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "not_found", "message", ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Map<String, Object>> handleUploadTooLarge(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("error", "payload_too_large", "message", "Image file exceeds the upload limit"));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MissingServletRequestPartException.class})
    public ResponseEntity<Map<String, Object>> handleMissingRequestValue(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "validation_error", "message", "A required request value is missing"));
    }

    @ExceptionHandler(MediaStorageException.class)
    public ResponseEntity<Map<String, Object>> handleMediaStorageFailure(MediaStorageException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "media_storage_error", "message", "Media storage is unavailable"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "validation_error", "message", message));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "bad_request", "message", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadableRequest(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "invalid_request", "message", "Request body contains invalid or malformed values"));
    }
}
