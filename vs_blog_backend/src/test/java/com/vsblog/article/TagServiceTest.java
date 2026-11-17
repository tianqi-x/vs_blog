package com.vsblog.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @InjectMocks
    private TagService tagService;

    @Test
    void createsNormalizedTagSlug() {
        given(tagRepository.existsBySlug("java-notes")).willReturn(false);
        given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));

        TagResponse created = tagService.create(new TagRequest(" Java Notes ", " Java   Notes "));

        assertEquals("Java Notes", created.name());
        assertEquals("java-notes", created.slug());
    }

    @Test
    void createsSlugFromChineseTagNameWhenSlugIsBlank() {
        given(tagRepository.existsBySlug("技术")).willReturn(false);
        given(tagRepository.save(any(Tag.class))).willAnswer(invocation -> invocation.getArgument(0));

        TagResponse created = tagService.create(new TagRequest("技术", ""));

        assertEquals("技术", created.slug());
    }

    @Test
    void rejectsDuplicateTagSlug() {
        given(tagRepository.existsBySlug("java")).willReturn(true);

        assertThrows(DuplicateTagSlugException.class, () -> tagService.create(new TagRequest("Java", "java")));

        verify(tagRepository, never()).save(any(Tag.class));
    }

    @Test
    void publicTagsOnlyIncludeTagsUsedByPublishedPublicArticles() {
        given(tagRepository.findPublicTags(ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC))
                .willReturn(List.of(new Tag("Java", "java")));

        tagService.listPublic();

        verify(tagRepository, times(1))
                .findPublicTags(ArticleStatus.PUBLISHED, ArticleVisibility.PUBLIC);
    }
}
