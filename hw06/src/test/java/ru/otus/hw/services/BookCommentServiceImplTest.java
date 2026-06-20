package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.otus.hw.converters.BookCommentConverter;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Сервис для работы с комментариями к книгам ")
@SpringBootTest
class BookCommentServiceImplTest {
    @Autowired
    private BookCommentService bookCommentService;

    @Autowired
    private BookCommentConverter bookCommentConverter;

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
}
