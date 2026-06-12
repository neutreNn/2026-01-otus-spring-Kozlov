package ru.otus.hw.repositories;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("Репозиторий на основе Jpa для работы с книгами ")
@DataJpaTest
@Import(JpaBookRepository.class)
class JpaBookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @DisplayName("должен загружать книгу по id")
    @Test
    void shouldReturnCorrectBookById() {
        var actualBook = bookRepository.findById(1L);

        assertThat(actualBook).isPresent();
        assertBook(actualBook.get(), 1L, "BookTitle_1", 1L, "Author_1", 1L, "Genre_1");
        assertBookRelationsAreLoaded(actualBook.get());
    }

    @DisplayName("должен загружать список всех книг с авторами и жанрами")
    @Test
    void shouldReturnCorrectBooksList() {
        var actualBooks = bookRepository.findAll();

        assertThat(actualBooks)
                .hasSize(3)
                .extracting(Book::getId, Book::getTitle)
                .containsExactly(
                        tuple(1L, "BookTitle_1"),
                        tuple(2L, "BookTitle_2"),
                        tuple(3L, "BookTitle_3"));
        actualBooks.forEach(this::assertBookRelationsAreLoaded);
    }

    @DisplayName("должен сохранять новую книгу")
    @Test
    void shouldSaveNewBook() {
        var author = entityManager.find(Author.class, 1L);
        var genre = entityManager.find(Genre.class, 1L);
        var book = new Book(0, "BookTitle_10500", author, genre);

        var savedBook = bookRepository.save(book);
        entityManager.flush();
        entityManager.clear();

        var actualBook = entityManager.find(Book.class, savedBook.getId());
        assertThat(savedBook.getId()).isPositive();
        assertBook(actualBook, savedBook.getId(), "BookTitle_10500", 1L, "Author_1", 1L, "Genre_1");
    }

    @DisplayName("должен сохранять измененную книгу")
    @Test
    void shouldSaveUpdatedBook() {
        var author = entityManager.find(Author.class, 3L);
        var genre = entityManager.find(Genre.class, 3L);
        var updatedBook = new Book(1L, "BookTitle_10500", author, genre);

        var savedBook = bookRepository.save(updatedBook);
        entityManager.flush();
        entityManager.clear();

        var actualBook = entityManager.find(Book.class, savedBook.getId());
        assertBook(actualBook, 1L, "BookTitle_10500", 3L, "Author_3", 3L, "Genre_3");
    }

    @DisplayName("должен удалять книгу по id")
    @Test
    void shouldDeleteBook() {
        assertThat(entityManager.find(Book.class, 1L)).isNotNull();

        bookRepository.deleteById(1L);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Book.class, 1L)).isNull();
    }

    @DisplayName("должен возвращать пустой Optional для отсутствующей книги")
    @Test
    void shouldReturnEmptyOptionalForMissingBook() {
        assertThat(bookRepository.findById(404)).isEmpty();
    }

    private void assertBook(Book actualBook, long id, String title, long authorId, String authorName,
                            long genreId, String genreName) {
        assertThat(actualBook).isNotNull();
        assertThat(actualBook.getId()).isEqualTo(id);
        assertThat(actualBook.getTitle()).isEqualTo(title);
        assertThat(actualBook.getAuthor().getId()).isEqualTo(authorId);
        assertThat(actualBook.getAuthor().getFullName()).isEqualTo(authorName);
        assertThat(actualBook.getGenre().getId()).isEqualTo(genreId);
        assertThat(actualBook.getGenre().getName()).isEqualTo(genreName);
    }

    private void assertBookRelationsAreLoaded(Book book) {
        var persistenceUnitUtil = entityManagerFactory.getPersistenceUnitUtil();
        assertThat(persistenceUnitUtil.isLoaded(book.getAuthor())).isTrue();
        assertThat(persistenceUnitUtil.isLoaded(book.getGenre())).isTrue();
    }
}
