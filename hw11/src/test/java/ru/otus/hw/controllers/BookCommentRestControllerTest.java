package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("REST-контроллер комментариев книги")
@WebFluxTest(BookCommentRestController.class)
class BookCommentRestControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookCommentService bookCommentService;

    @DisplayName("должен возвращать комментарии книги")
    @Test
    void shouldReturnBookComments() {
        when(bookService.findById(new BookIdDto(1L))).thenReturn(Mono.just(bookDto(1L)));
        when(bookCommentService.findByBookId(1L)).thenReturn(Flux.just(
                new BookComment(1L, "Comment_1", 1L),
                new BookComment(2L, "Comment_2", 1L)
        ));

        webTestClient.get().uri("/api/books/1/comments")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo(1)
                .jsonPath("$[0].text").isEqualTo("Comment_1")
                .jsonPath("$[1].id").isEqualTo(2)
                .jsonPath("$[1].text").isEqualTo("Comment_2");

        verify(bookService).findById(new BookIdDto(1L));
        verify(bookCommentService).findByBookId(1L);
    }

    @DisplayName("должен возвращать 404 для комментариев отсутствующей книги")
    @Test
    void shouldReturnNotFoundForMissingBookComments() {
        when(bookService.findById(new BookIdDto(404L)))
                .thenReturn(Mono.error(new EntityNotFoundException("Book with id 404 not found")));

        webTestClient.get().uri("/api/books/404/comments")
                .exchange()
                .expectStatus().isNotFound();

        verifyNoInteractions(bookCommentService);
    }

    private static BookDto bookDto(long id) {
        return new BookDto(id, "BookTitle_%d".formatted(id), 1L, "Author_1", 1L, "Genre_1");
    }
}
