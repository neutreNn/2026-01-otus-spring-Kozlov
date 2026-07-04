package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("MVC-контроллер страниц книг")
@WebFluxTest(BookController.class)
class BookControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @DisplayName("должен перенаправлять с главной страницы на список книг")
    @Test
    void shouldRedirectRootToBooksList() {
        webTestClient.get().uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/books");
    }

    @DisplayName("должен отображать страницу списка книг")
    @Test
    void shouldRenderBooksListPage() {
        webTestClient.get().uri("/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> assertThat(result.getResponseBody())
                        .contains("/js/books-list.js", "data-books-table"));
    }

    @DisplayName("должен отображать страницу книги с идентификатором для AJAX-загрузки")
    @Test
    void shouldRenderBookDetailsPage() {
        webTestClient.get().uri("/books/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> assertThat(result.getResponseBody())
                        .contains("/js/book-details.js", "data-book-id=\"1\""));
    }

    @DisplayName("должен отображать страницу создания книги")
    @Test
    void shouldRenderNewBookPage() {
        webTestClient.get().uri("/books/new")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> assertThat(result.getResponseBody())
                        .contains("/js/book-form.js", "Новая книга"));
    }

    @DisplayName("должен отображать страницу редактирования книги с идентификатором для AJAX-загрузки")
    @Test
    void shouldRenderEditBookPage() {
        webTestClient.get().uri("/books/1/edit")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(result -> assertThat(result.getResponseBody())
                        .contains("data-book-id=\"1\"", "Книга #1"));
    }

    @DisplayName("не должен выполнять создание книги через MVC POST")
    @Test
    void shouldNotCreateBookByMvcPost() {
        webTestClient.post().uri("/books")
                .exchange()
                .expectStatus().isEqualTo(405);
    }
}
