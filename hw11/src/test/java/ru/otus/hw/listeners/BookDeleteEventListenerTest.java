package ru.otus.hw.listeners;

import org.bson.Document;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.mapping.event.AfterDeleteEvent;
import reactor.core.publisher.Mono;
import ru.otus.hw.models.Book;
import ru.otus.hw.repositories.BookCommentRepository;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Mongo-обработчик удаления книги")
class BookDeleteEventListenerTest {
    @DisplayName("должен удалять комментарии после удаления книги")
    @Test
    void shouldDeleteCommentsAfterBookDelete() {
        var bookCommentRepository = mock(BookCommentRepository.class);
        var listener = new BookDeleteEventListener(bookCommentRepository);
        when(bookCommentRepository.deleteByBookId(1L)).thenReturn(Mono.just(2L));

        listener.onAfterDelete(new AfterDeleteEvent<>(new Document("_id", 1L), Book.class, "books"));

        verify(bookCommentRepository).deleteByBookId(1L);
    }

    @DisplayName("не должен удалять комментарии, если в событии нет идентификатора книги")
    @Test
    void shouldNotDeleteCommentsWithoutBookId() {
        var bookCommentRepository = mock(BookCommentRepository.class);
        var listener = new BookDeleteEventListener(bookCommentRepository);

        listener.onAfterDelete(new AfterDeleteEvent<>(new Document("title", "BookTitle_1"), Book.class, "books"));

        verifyNoInteractions(bookCommentRepository);
    }
}
