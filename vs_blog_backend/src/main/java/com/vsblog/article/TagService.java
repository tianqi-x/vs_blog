package com.vsblog.article;

import java.util.List;
import java.util.UUID;

import com.vsblog.util.SlugGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TagService {

    private final TagRepository tagRepository;

    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    public List<TagResponse> listAll() {
        return tagRepository.findAllByOrderByNameAsc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<TagResponse> listPublic() {
        return tagRepository.findPublicTags(ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TagResponse getById(UUID id) {
        return tagRepository.findById(id).map(this::toResponse).orElseThrow(() -> new TagNotFoundException(id));
    }

    @Transactional
    public TagResponse create(TagRequest request) {
        String name = request.name().trim();
        String slug = normalizeSlug(request.slug(), name);
        ensureUniqueSlug(slug, null);
        return toResponse(tagRepository.save(new Tag(name, slug)));
    }

    @Transactional
    public TagResponse update(UUID id, TagRequest request) {
        Tag tag = tagRepository.findById(id).orElseThrow(() -> new TagNotFoundException(id));
        String name = request.name().trim();
        String slug = request.slug() == null || request.slug().isBlank()
                ? tag.getSlug()
                : normalizeSlug(request.slug(), name);
        ensureUniqueSlug(slug, id);
        tag.setName(name);
        tag.setSlug(slug);
        return toResponse(tagRepository.save(tag));
    }

    @Transactional
    public void delete(UUID id) {
        if (!tagRepository.existsById(id)) {
            throw new TagNotFoundException(id);
        }
        tagRepository.deleteById(id);
    }

    private void ensureUniqueSlug(String slug, UUID ignoredTagId) {
        boolean exists = ignoredTagId == null
                ? tagRepository.existsBySlug(slug)
                : tagRepository.existsBySlugAndIdNot(slug, ignoredTagId);
        if (exists) {
            throw new DuplicateTagSlugException(slug);
        }
    }

    private String normalizeSlug(String value, String fallback) {
        return SlugGenerator.generate(value, fallback);
    }

    private TagResponse toResponse(Tag tag) {
        return new TagResponse(tag.getId(), tag.getName(), tag.getSlug(), tag.getCreatedAt(), tag.getUpdatedAt());
    }
}
