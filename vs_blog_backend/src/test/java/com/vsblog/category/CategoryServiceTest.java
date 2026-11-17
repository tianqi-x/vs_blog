package com.vsblog.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import com.vsblog.article.ArticleStatus;
import com.vsblog.article.ArticleVisibility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void publicCategoriesOnlyIncludeCategoriesUsedByPublishedPublicArticles() {
        Category category = new Category("Java", "java", null, 0);
        given(categoryRepository.findPublicCategories(ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC))
                .willReturn(List.of(category));

        List<CategoryResponse> result = categoryService.listPublic();

        assertEquals("java", result.get(0).slug());
        verify(categoryRepository).findPublicCategories(ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC);
    }

    @Test
    void createGeneratesSlugFromChineseNameWhenSlugIsBlank() {
        given(categoryRepository.existsBySlug("技术-分类")).willReturn(false);
        given(categoryRepository.save(any(Category.class))).willAnswer(invocation -> invocation.getArgument(0));

        CategoryResponse response = categoryService.create(
                new CategoryCreateRequest("技术 分类", "", null, null));

        assertEquals("技术-分类", response.slug());
    }
}
