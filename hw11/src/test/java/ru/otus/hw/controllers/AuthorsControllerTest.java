package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import ru.otus.hw.models.Author;
import ru.otus.hw.services.AuthorService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Контроллер авторов")
@WebFluxTest(AuthorsController.class)
class AuthorsControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private AuthorService authorService;

    @DisplayName("должен отображать список авторов")
    @Test
    void shouldRenderAuthorsList() {
        when(authorService.findAll()).thenReturn(Flux.just(
                new Author(1L, "Author_1"),
                new Author(2L, "Author_2")
        ));

        webTestClient.get().uri("/authors")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> assertThat(result.getResponseBody())
                        .contains("Author_1", "Author_2"));
    }
}
