package ru.otus.hw.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("Сервис для работы с авторами ")
class AuthorServiceImplTest extends ServiceMongoTestBase {
    @Autowired
    private AuthorService authorService;

    @DisplayName("должен возвращать всех авторов")
    @Test
    void shouldReturnAllAuthors() {
        assertThat(authorService.findAll())
                .extracting("id", "fullName")
                .containsExactly(
                        tuple(1L, "Author_1"),
                        tuple(2L, "Author_2"),
                        tuple(3L, "Author_3"));
    }
}
