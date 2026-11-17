package com.vsblog.article;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ArticleNoteServiceTest {

    @Mock
    private ArticleNoteRepository noteRepository;

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleNoteService noteService;

    @Test
    void createsNoteScopedToExistingArticle() {
        UUID articleId = UUID.randomUUID();
        given(articleRepository.existsById(articleId)).willReturn(true);
        given(noteRepository.save(any(ArticleNote.class))).willAnswer(invocation -> {
            ArticleNote note = invocation.getArgument(0);
            assertEquals(articleId, note.getArticleId());
            return note;
        });

        ArticleNoteResponse response = noteService.create(articleId, new ArticleNoteRequest("Personal takeaway"));

        assertEquals(articleId, response.articleId());
        assertEquals("Personal takeaway", response.content());
    }

    @Test
    void rejectsNotesForMissingArticles() {
        UUID articleId = UUID.randomUUID();
        given(articleRepository.existsById(articleId)).willReturn(false);

        assertThrows(ArticleNotFoundException.class,
                () -> noteService.list(articleId));
    }

    @Test
    void noteCannotBeAccessedThroughAnotherArticleId() {
        UUID articleId = UUID.randomUUID();
        UUID otherArticleId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        given(articleRepository.existsById(otherArticleId)).willReturn(true);
        given(noteRepository.findByIdAndArticleId(noteId, otherArticleId)).willReturn(Optional.empty());

        assertThrows(ArticleNoteNotFoundException.class,
                () -> noteService.update(otherArticleId, noteId, new ArticleNoteRequest("changed")));

        verify(noteRepository).findByIdAndArticleId(noteId, otherArticleId);
    }

    @Test
    void updateAndDeleteOperateOnlyOnTheNoteUnderItsArticle() {
        UUID articleId = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        ArticleNote note = new ArticleNote(articleId, "before");
        ReflectionTestUtils.setField(note, "id", noteId);
        given(articleRepository.existsById(articleId)).willReturn(true);
        given(noteRepository.findByIdAndArticleId(noteId, articleId)).willReturn(Optional.of(note));
        given(noteRepository.save(note)).willReturn(note);

        ArticleNoteResponse updated = noteService.update(articleId, noteId, new ArticleNoteRequest("after"));
        noteService.delete(articleId, noteId);

        assertEquals("after", updated.content());
        verify(noteRepository).delete(note);
    }
}
