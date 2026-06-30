package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.GenreService;

import static org.mockito.Mockito.when;

@DisplayName("REST-контроллер справочников")
@WebFluxTest(ReferenceDataRestController.class)
class ReferenceDataRestControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    @DisplayName("должен возвращать список авторов")
    @Test
    void shouldReturnAuthors() {
        when(authorService.findAll()).thenReturn(Flux.just(new Author(1L, "Author_1"), new Author(2L, "Author_2")));

        webTestClient.get().uri("/api/authors")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].fullName").isEqualTo("Author_1")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].fullName").isEqualTo("Author_2");
    }

    @DisplayName("должен возвращать список жанров")
    @Test
    void shouldReturnGenres() {
        when(genreService.findAll()).thenReturn(Flux.just(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2")));

        webTestClient.get().uri("/api/genres")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].name").isEqualTo("Genre_1")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].name").isEqualTo("Genre_2");
    }
}
