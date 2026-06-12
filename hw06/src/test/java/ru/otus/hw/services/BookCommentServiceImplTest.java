package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.models.BookComment;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Сервис для работы с комментариями к книгам ")
@SpringBootTest
class BookCommentServiceImplTest {
    @Autowired
    private BookCommentService bookCommentService;

    @DisplayName("должен возвращать комментарий со связями, доступными вне сервисного метода")
    @Test
    void shouldReturnCommentWithRelationsAvailableOutsideServiceMethod() {
        var actualComment = bookCommentService.findById(1L);

        assertThat(actualComment).isPresent();
        assertCommentRelations(actualComment.get(), "BookTitle_1", "Author_1", "Genre_1");
    }

    @DisplayName("должен возвращать комментарии книги со связями, доступными вне сервисного метода")
    @Test
    void shouldReturnCommentsWithRelationsAvailableOutsideServiceMethod() {
        var actualComments = bookCommentService.findByBookId(1L);

        assertThat(actualComments).hasSize(2);
        actualComments.forEach(comment -> assertCommentRelations(comment, "BookTitle_1", "Author_1", "Genre_1"));
    }

    private void assertCommentRelations(BookComment comment, String bookTitle, String authorName, String genreName) {
        assertThat(comment.getBook().getTitle()).isEqualTo(bookTitle);
        assertThat(comment.getBook().getAuthor().getFullName()).isEqualTo(authorName);
        assertThat(comment.getBook().getGenre().getName()).isEqualTo(genreName);
    }
}
