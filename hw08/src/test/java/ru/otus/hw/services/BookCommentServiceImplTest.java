package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.otus.hw.converters.BookCommentConverter;
import ru.otus.hw.exceptions.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Сервис для работы с комментариями к книгам ")
class BookCommentServiceImplTest extends ServiceMongoTestBase {
    @Autowired
    private BookCommentService bookCommentService;

    private final BookCommentConverter bookCommentConverter = new BookCommentConverter();

    @DisplayName("должен возвращать комментарий, доступный вне сервисного метода")
    @Test
    void shouldReturnCommentAvailableOutsideServiceMethod() {
        var actualComment = bookCommentService.findById(1L);

        assertThat(actualComment).isPresent();
        assertThat(bookCommentConverter.commentToString(actualComment.get()))
                .isEqualTo("Id: 1, text: Comment_1");
    }

    @DisplayName("должен возвращать комментарии книги, доступные вне сервисного метода")
    @Test
    void shouldReturnCommentsAvailableOutsideServiceMethod() {
        var actualComments = bookCommentService.findByBookId(1L);

        assertThat(actualComments).hasSize(2);
        assertThat(actualComments)
                .map(bookCommentConverter::commentToString)
                .containsExactly("Id: 1, text: Comment_1", "Id: 2, text: Comment_2");
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldInsertComment() {
        var actualComment = bookCommentService.insert("Comment_4", 1L);

        assertThat(actualComment.getId()).isEqualTo(4L);
        assertThat(actualComment.getText()).isEqualTo("Comment_4");
        assertThat(actualComment.getBookId()).isEqualTo(1L);
    }

    @DisplayName("должен обновлять комментарий")
    @Test
    void shouldUpdateComment() {
        var actualComment = bookCommentService.update(1L, "UpdatedComment");

        assertThat(actualComment.getId()).isEqualTo(1L);
        assertThat(actualComment.getText()).isEqualTo("UpdatedComment");
        assertThat(actualComment.getBookId()).isEqualTo(1L);
    }

    @DisplayName("должен удалять комментарий")
    @Test
    void shouldDeleteComment() {
        bookCommentService.deleteById(1L);

        assertThat(bookCommentService.findById(1L)).isEmpty();
    }

    @DisplayName("должен сообщать об отсутствующей книге при сохранении комментария")
    @Test
    void shouldThrowExceptionWhenBookNotFoundOnInsert() {
        assertThatThrownBy(() -> bookCommentService.insert("Comment_4", 404L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Book with id 404 not found");
    }
}
