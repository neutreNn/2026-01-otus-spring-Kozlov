package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Сервис для работы с книгами ")
class BookServiceImplTest extends ServiceMongoTestBase {
    @Autowired
    private BookService bookService;

    @DisplayName("должен возвращать книгу со связями, доступными вне сервисного метода")
    @Test
    void shouldReturnBookWithRelationsAvailableOutsideServiceMethod() {
        var actualBook = bookService.findById(1L);

        assertThat(actualBook).isPresent();
        assertBookRelations(actualBook.get(), "Author_1", "Genre_1");
    }

    @DisplayName("должен возвращать список книг со связями, доступными вне сервисного метода")
    @Test
    void shouldReturnBooksWithRelationsAvailableOutsideServiceMethod() {
        var actualBooks = bookService.findAll();

        assertThat(actualBooks).hasSize(3);
        assertBookRelations(actualBooks.get(0), "Author_1", "Genre_1");
        assertBookRelations(actualBooks.get(1), "Author_2", "Genre_2");
        assertBookRelations(actualBooks.get(2), "Author_3", "Genre_3");
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldInsertBook() {
        var actualBook = bookService.insert("BookTitle_4", 1L, 2L);

        assertThat(actualBook.getId()).isEqualTo(4L);
        assertThat(actualBook.getTitle()).isEqualTo("BookTitle_4");
        assertBookRelations(actualBook, "Author_1", "Genre_2");
        assertThat(bookRepository.findById(4L)).isPresent();
    }

    @DisplayName("должен обновлять книгу")
    @Test
    void shouldUpdateBook() {
        var actualBook = bookService.update(1L, "UpdatedBookTitle", 3L, 2L);

        assertThat(actualBook.getId()).isEqualTo(1L);
        assertThat(actualBook.getTitle()).isEqualTo("UpdatedBookTitle");
        assertBookRelations(actualBook, "Author_3", "Genre_2");
    }

    @DisplayName("должен удалять книгу вместе с комментариями")
    @Test
    void shouldDeleteBookWithComments() {
        bookService.deleteById(1L);

        assertThat(bookRepository.findById(1L)).isEmpty();
        assertThat(bookCommentRepository.findByBookId(1L, Sort.by("id"))).isEmpty();
    }

    @DisplayName("должен сообщать об отсутствующем авторе при сохранении книги")
    @Test
    void shouldThrowExceptionWhenAuthorNotFoundOnInsert() {
        assertThatThrownBy(() -> bookService.insert("BookTitle_4", 404L, 1L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Author with id 404 not found");
    }

    private void assertBookRelations(Book book, String authorName, String genreName) {
        assertThat(book.getAuthor().getFullName()).isEqualTo(authorName);
        assertThat(book.getGenre().getName()).isEqualTo(genreName);
    }
}
