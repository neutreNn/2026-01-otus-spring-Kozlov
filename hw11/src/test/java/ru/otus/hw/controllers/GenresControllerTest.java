package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Контроллер жанров")
@WebFluxTest(GenresController.class)
class GenresControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private GenreService genreService;

    @DisplayName("должен отображать список жанров")
    @Test
    void shouldRenderGenresList() {
        when(genreService.findAll()).thenReturn(Flux.just(
                new Genre(1L, "Genre_1"),
                new Genre(2L, "Genre_2")
        ));

        webTestClient.get().uri("/genres")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> assertThat(result.getResponseBody())
                        .contains("Genre_1", "Genre_2"));
    }
}
