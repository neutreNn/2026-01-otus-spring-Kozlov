package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.otus.hw.models.Author;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("Репозиторий Spring Data JPA для работы с авторами ")
@DataJpaTest
class AuthorRepositoryTest {
    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private TestEntityManager entityManager;

    @DisplayName("должен загружать автора по id")
    @Test
    void shouldReturnCorrectAuthorById() {
        var expectedAuthor = entityManager.find(Author.class, 1L);

        assertThat(authorRepository.findById(expectedAuthor.getId()))
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expectedAuthor);
    }

    @DisplayName("должен загружать список всех авторов")
    @Test
    void shouldReturnCorrectAuthorsList() {
        var expectedAuthors = List.of(
                entityManager.find(Author.class, 1L),
                entityManager.find(Author.class, 2L),
                entityManager.find(Author.class, 3L));

        var actualAuthors = authorRepository.findAll();

        assertThat(actualAuthors).containsExactlyElementsOf(expectedAuthors);
        assertThat(actualAuthors)
                .extracting(Author::getId, Author::getFullName)
                .containsExactly(
                        tuple(1L, "Author_1"),
                        tuple(2L, "Author_2"),
                        tuple(3L, "Author_3"));
    }

    @DisplayName("должен возвращать пустой Optional для отсутствующего автора")
    @Test
    void shouldReturnEmptyOptionalForMissingAuthor() {
        assertThat(authorRepository.findById(404L)).isEmpty();
    }
}
