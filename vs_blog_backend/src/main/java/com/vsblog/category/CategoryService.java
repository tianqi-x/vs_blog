package com.vsblog.category;

import java.util.List;
import java.util.UUID;

import com.vsblog.article.ArticleStatus;
import com.vsblog.article.ArticleVisibility;
import com.vsblog.util.SlugGenerator;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryResponse> listAll() {
        return categoryRepository.findAll(Sort.by("sortOrder").ascending().and(Sort.by("name").ascending()))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listPublic() {
        return categoryRepository.findPublicCategories(ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public CategoryResponse getById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));
        return toResponse(category);
    }

    @Transactional
    public CategoryResponse create(CategoryCreateRequest request) {
        String name = normalizeName(request.name());
        String slug = normalizeSlug(request.slug(), name);
        ensureUniqueSlug(slug, null);

        Category category = new Category(
                name,
                slug,
                request.description(),
                request.sortOrder() == null ? 0 : request.sortOrder());

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        String nextName = request.name() == null ? category.getName() : normalizeName(request.name());
        String nextSlug = request.slug() == null ? category.getSlug() : normalizeSlug(request.slug(), nextName);
        ensureUniqueSlug(nextSlug, id);

        category.setName(nextName);
        category.setSlug(nextSlug);
        category.setDescription(request.description() == null ? category.getDescription() : request.description());
        category.setSortOrder(request.sortOrder() == null ? category.getSortOrder() : request.sortOrder());

        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void delete(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new CategoryNotFoundException(id);
        }
        categoryRepository.deleteById(id);
    }

    private void ensureUniqueSlug(String slug, UUID ignoredCategoryId) {
        boolean exists = ignoredCategoryId == null
                ? categoryRepository.existsBySlug(slug)
                : categoryRepository.existsBySlugAndIdNot(slug, ignoredCategoryId);
        if (exists) {
            throw new DuplicateSlugException(slug);
        }
    }

    private String normalizeName(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }
        return value.trim();
    }

    private String normalizeSlug(String value, String fallback) {
        return SlugGenerator.generate(value, fallback);
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getSlug(),
                category.getDescription(),
                category.getSortOrder(),
                category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
