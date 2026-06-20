package ru.otus.hw.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.otus.hw.models.Genre;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("Репозиторий на основе Jpa для работы с жанрами ")
@DataJpaTest
@Import(JpaGenreRepository.class)
class JpaGenreRepositoryTest {
    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private TestEntityManager entityManager;

    @DisplayName("должен загружать жанр по id")
    @Test
    void shouldReturnCorrectGenreById() {
        var expectedGenre = entityManager.find(Genre.class, 1L);

        assertThat(genreRepository.findById(expectedGenre.getId()))
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(expectedGenre);
    }

    @DisplayName("должен загружать список всех жанров")
    @Test
    void shouldReturnCorrectGenresList() {
        var expectedGenres = List.of(
                entityManager.find(Genre.class, 1L),
                entityManager.find(Genre.class, 2L),
                entityManager.find(Genre.class, 3L));

        var actualGenres = genreRepository.findAll();

        assertThat(actualGenres).containsExactlyElementsOf(expectedGenres);
        assertThat(actualGenres)
                .extracting(Genre::getId, Genre::getName)
                .containsExactly(
                        tuple(1L, "Genre_1"),
                        tuple(2L, "Genre_2"),
                        tuple(3L, "Genre_3"));
    }

    @DisplayName("должен возвращать пустой Optional для отсутствующего жанра")
    @Test
    void shouldReturnEmptyOptionalForMissingGenre() {
        assertThat(genreRepository.findById(404)).isEmpty();
    }
}
