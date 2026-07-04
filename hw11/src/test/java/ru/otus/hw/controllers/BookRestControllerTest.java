package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("REST-контроллер книг")
@WebFluxTest(BookRestController.class)
class BookRestControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookService bookService;

    @DisplayName("должен возвращать список книг")
    @Test
    void shouldReturnAllBooks() {
        when(bookService.findAll()).thenReturn(Flux.just(bookDto(1L, "BookTitle_1", 1L, 1L)));

        webTestClient.get().uri("/api/books")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].title").isEqualTo("BookTitle_1")
                .jsonPath("$[0].authorFullName").isEqualTo("Author_1")
                .jsonPath("$[0].genreName").isEqualTo("Genre_1");
    }

    @DisplayName("должен возвращать книгу по идентификатору")
    @Test
    void shouldReturnBookById() {
        when(bookService.findById(new BookIdDto(1L))).thenReturn(Mono.just(bookDto(1L, "BookTitle_1", 1L, 1L)));

        webTestClient.get().uri("/api/books/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("BookTitle_1")
                .jsonPath("$.authorId").isEqualTo(1)
                .jsonPath("$.genreId").isEqualTo(1);
    }

    @DisplayName("должен возвращать 404 для отсутствующей книги")
    @Test
    void shouldReturnNotFoundForMissingBook() {
        when(bookService.findById(new BookIdDto(404L)))
                .thenReturn(Mono.error(new EntityNotFoundException("Book with id 404 not found")));

        webTestClient.get().uri("/api/books/404")
                .exchange()
                .expectStatus().isNotFound();
    }

    @DisplayName("должен создавать книгу")
    @Test
    void shouldCreateBook() {
        var bookCreateDto = new BookCreateDto("  New Book  ", 1L, 2L);
        var savedBook = bookDto(4L, "New Book", 1L, 2L);
        when(bookService.insert(bookCreateDto)).thenReturn(Mono.just(savedBook));

        webTestClient.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookCreateDto)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Location", "/api/books/4")
                .expectBody()
                .jsonPath("$.id").isEqualTo(4)
                .jsonPath("$.title").isEqualTo("New Book");

        verify(bookService).insert(bookCreateDto);
    }

    @DisplayName("должен возвращать 400 при ошибках валидации создания")
    @Test
    void shouldReturnBadRequestForInvalidCreateRequest() {
        webTestClient.post().uri("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "title": " ",
                          "authorId": 0,
                          "genreId": null
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.fieldErrors.title").exists()
                .jsonPath("$.fieldErrors.authorId").exists()
                .jsonPath("$.fieldErrors.genreId").exists();

        verifyNoInteractions(bookService);
    }

    @DisplayName("должен обновлять книгу")
    @Test
    void shouldUpdateBook() {
        var request = new BookCreateDto("  Updated Book  ", 2L, 3L);
        var updateDto = new BookUpdateDto(1L, "  Updated Book  ", 2L, 3L);
        when(bookService.update(updateDto)).thenReturn(Mono.just(bookDto(1L, "Updated Book", 2L, 3L)));

        webTestClient.put().uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.title").isEqualTo("Updated Book")
                .jsonPath("$.authorId").isEqualTo(2)
                .jsonPath("$.genreId").isEqualTo(3);

        verify(bookService).update(updateDto);
    }

    @DisplayName("должен возвращать 400 при ошибках валидации обновления")
    @Test
    void shouldReturnBadRequestForInvalidUpdateRequest() {
        webTestClient.put().uri("/api/books/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "title": "",
                          "authorId": null,
                          "genreId": 0
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.fieldErrors.title").exists()
                .jsonPath("$.fieldErrors.authorId").exists()
                .jsonPath("$.fieldErrors.genreId").exists();

        verifyNoInteractions(bookService);
    }

    @DisplayName("должен удалять книгу")
    @Test
    void shouldDeleteBook() {
        when(bookService.deleteById(new BookIdDto(1L))).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/books/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(bookService).deleteById(new BookIdDto(1L));
    }

    @DisplayName("не должен удалять книгу POST-запросом")
    @Test
    void shouldNotDeleteBookByPost() {
        webTestClient.post().uri("/api/books/1")
                .exchange()
                .expectStatus().isEqualTo(405);

        verifyNoInteractions(bookService);
    }

    private static BookDto bookDto(long id, String title, long authorId, long genreId) {
        return new BookDto(
                id,
                title,
                authorId,
                "Author_%d".formatted(authorId),
                genreId,
                "Genre_%d".formatted(genreId)
        );
    }
}
