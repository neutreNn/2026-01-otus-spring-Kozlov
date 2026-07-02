package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookIdDto;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Сервис для работы с книгами ")
@DataJpaTest
@Import(BookServiceImpl.class)
@Transactional(propagation = Propagation.NEVER)
class BookServiceImplTest {
    @Autowired
    private BookService bookService;

    @DisplayName("должен возвращать книгу со связями, доступными вне сервисного метода")
    @Test
    void shouldReturnBookWithRelationsAvailableOutsideServiceMethod() {
        var actualBook = bookService.findById(new BookIdDto(1L));

        assertBookRelations(actualBook, "Author_1", "Genre_1");
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

    private void assertBookRelations(BookDto book, String authorName, String genreName) {
        assertThat(book.getAuthorFullName()).isEqualTo(authorName);
        assertThat(book.getGenreName()).isEqualTo(genreName);
    }
}
