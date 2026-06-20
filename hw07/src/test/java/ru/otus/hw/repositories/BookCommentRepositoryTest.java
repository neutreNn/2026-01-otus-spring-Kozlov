package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("Репозиторий Spring Data JPA для работы с комментариями к книгам ")
@DataJpaTest
class BookCommentRepositoryTest {
    @Autowired
    private BookCommentRepository bookCommentRepository;

    @Autowired
    private TestEntityManager entityManager;

    @DisplayName("должен загружать комментарий по id")
    @Test
    void shouldReturnCorrectCommentById() {
        var actualComment = bookCommentRepository.findById(1L);

        assertThat(actualComment).isPresent();
        assertComment(actualComment.get(), 1L, "Comment_1");
    }

    @DisplayName("должен загружать комментарии по id книги")
    @Test
    void shouldReturnCorrectCommentsByBookId() {
        var actualComments = bookCommentRepository.findByBookId(1L);

        assertThat(actualComments)
                .hasSize(2)
                .extracting(BookComment::getId, BookComment::getText)
                .containsExactly(
                        tuple(1L, "Comment_1"),
                        tuple(2L, "Comment_2"));
    }

    @DisplayName("должен сохранять новый комментарий")
    @Test
    void shouldSaveNewComment() {
        var book = entityManager.find(Book.class, 1L);
        var comment = new BookComment(0, "Comment_10500", book);

        var savedComment = bookCommentRepository.save(comment);
        entityManager.flush();
        entityManager.clear();

        var actualComment = entityManager.find(BookComment.class, savedComment.getId());
        assertThat(savedComment.getId()).isPositive();
        assertComment(actualComment, savedComment.getId(), "Comment_10500");
        assertThat(actualComment.getBook().getId()).isEqualTo(1L);
    }

    @DisplayName("должен сохранять измененный комментарий")
    @Test
    void shouldSaveUpdatedComment() {
        var book = entityManager.find(Book.class, 1L);
        var updatedComment = new BookComment(1L, "Comment_10500", book);

        var savedComment = bookCommentRepository.save(updatedComment);
        entityManager.flush();
        entityManager.clear();

        var actualComment = entityManager.find(BookComment.class, savedComment.getId());
        assertComment(actualComment, 1L, "Comment_10500");
        assertThat(actualComment.getBook().getId()).isEqualTo(1L);
    }

    @DisplayName("должен удалять комментарий по id")
    @Test
    void shouldDeleteComment() {
        assertThat(entityManager.find(BookComment.class, 1L)).isNotNull();

        bookCommentRepository.deleteById(1L);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(BookComment.class, 1L)).isNull();
    }

    @DisplayName("должен возвращать пустой Optional для отсутствующего комментария")
    @Test
    void shouldReturnEmptyOptionalForMissingComment() {
        assertThat(bookCommentRepository.findById(404L)).isEmpty();
    }

    private void assertComment(BookComment actualComment, long id, String text) {
        assertThat(actualComment).isNotNull();
        assertThat(actualComment.getId()).isEqualTo(id);
        assertThat(actualComment.getText()).isEqualTo(text);
    }
}
