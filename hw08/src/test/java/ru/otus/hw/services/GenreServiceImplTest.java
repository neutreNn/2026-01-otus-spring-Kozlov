package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("Сервис для работы с жанрами ")
class GenreServiceImplTest extends ServiceMongoTestBase {
    @Autowired
    private GenreService genreService;

    @DisplayName("должен возвращать все жанры")
    @Test
    void shouldReturnAllGenres() {
        assertThat(genreService.findAll())
                .extracting("id", "name")
                .containsExactly(
                        tuple(1L, "Genre_1"),
                        tuple(2L, "Genre_2"),
                        tuple(3L, "Genre_3"));
    }
}
