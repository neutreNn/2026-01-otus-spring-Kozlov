package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("REST-контроллер комментариев книги")
@WebMvcTest(BookCommentRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class BookCommentRestControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private BookCommentService bookCommentService;

    @DisplayName("должен возвращать комментарии книги")
    @Test
    void shouldReturnBookComments() throws Exception {
        when(bookService.findById(new BookIdDto(1L))).thenReturn(bookDto(1L));
        when(bookCommentService.findByBookId(1L)).thenReturn(List.of(
                new BookComment(1L, "Comment_1", bookEntity(1L)),
                new BookComment(2L, "Comment_2", bookEntity(1L))
        ));

        mvc.perform(get("/api/books/1/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("Comment_1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].text").value("Comment_2"));

        verify(bookService).findById(new BookIdDto(1L));
        verify(bookCommentService).findByBookId(1L);
    }

    @DisplayName("должен возвращать 404 для комментариев отсутствующей книги")
    @Test
    void shouldReturnNotFoundForMissingBookComments() throws Exception {
        when(bookService.findById(new BookIdDto(404L)))
                .thenThrow(new EntityNotFoundException("Book with id 404 not found"));

        mvc.perform(get("/api/books/404/comments"))
                .andExpect(status().isNotFound());

        verifyNoInteractions(bookCommentService);
    }

    private static BookDto bookDto(long id) {
        return new BookDto(id, "BookTitle_%d".formatted(id), 1L, "Author_1", 1L, "Genre_1");
    }

    private static Book bookEntity(long id) {
        return new Book(id, "BookTitle_%d".formatted(id), new Author(1L, "Author_1"), new Genre(1L, "Genre_1"));
    }
}
