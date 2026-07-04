package ru.otus.hw.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.dto.BookCreateDto;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookIdDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.services.BookService;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("REST-контроллер книг")
@WebMvcTest(BookRestController.class)
@WithMockUser
class BookRestControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @DisplayName("должен возвращать список книг")
    @Test
    void shouldReturnAllBooks() throws Exception {
        when(bookService.findAll()).thenReturn(List.of(bookDto(1L, "BookTitle_1", 1L, 1L)));

        mvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("BookTitle_1"))
                .andExpect(jsonPath("$[0].authorFullName").value("Author_1"))
                .andExpect(jsonPath("$[0].genreName").value("Genre_1"));
    }

    @DisplayName("должен возвращать книгу по идентификатору")
    @Test
    void shouldReturnBookById() throws Exception {
        when(bookService.findById(new BookIdDto(1L))).thenReturn(bookDto(1L, "BookTitle_1", 1L, 1L));

        mvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("BookTitle_1"))
                .andExpect(jsonPath("$.authorId").value(1))
                .andExpect(jsonPath("$.genreId").value(1));
    }

    @DisplayName("должен возвращать 404 для отсутствующей книги")
    @Test
    void shouldReturnNotFoundForMissingBook() throws Exception {
        when(bookService.findById(new BookIdDto(404L)))
                .thenThrow(new EntityNotFoundException("Book with id 404 not found"));

        mvc.perform(get("/api/books/404"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("должен создавать книгу")
    @Test
    void shouldCreateBook() throws Exception {
        var bookCreateDto = new BookCreateDto("  New Book  ", 1L, 2L);
        var savedBook = bookDto(4L, "New Book", 1L, 2L);
        when(bookService.insert(bookCreateDto)).thenReturn(savedBook);

        mvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/books/4"))
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.title").value("New Book"));

        verify(bookService).insert(bookCreateDto);
    }

    @DisplayName("должен возвращать 400 при ошибках валидации создания")
    @Test
    void shouldReturnBadRequestForInvalidCreateRequest() throws Exception {
        mvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": " ",
                                  "authorId": 0,
                                  "genreId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.authorId").exists())
                .andExpect(jsonPath("$.fieldErrors.genreId").exists());

        verifyNoInteractions(bookService);
    }

    @DisplayName("должен обновлять книгу")
    @Test
    void shouldUpdateBook() throws Exception {
        var request = new BookCreateDto("  Updated Book  ", 2L, 3L);
        var updateDto = new BookUpdateDto(1L, "  Updated Book  ", 2L, 3L);
        when(bookService.update(updateDto)).thenReturn(bookDto(1L, "Updated Book", 2L, 3L));

        mvc.perform(put("/api/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Book"))
                .andExpect(jsonPath("$.authorId").value(2))
                .andExpect(jsonPath("$.genreId").value(3));

        verify(bookService).update(updateDto);
    }

    @DisplayName("должен возвращать 400 при ошибках валидации обновления")
    @Test
    void shouldReturnBadRequestForInvalidUpdateRequest() throws Exception {
        mvc.perform(put("/api/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "authorId": null,
                                  "genreId": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists())
                .andExpect(jsonPath("$.fieldErrors.authorId").exists())
                .andExpect(jsonPath("$.fieldErrors.genreId").exists());

        verifyNoInteractions(bookService);
    }

    @DisplayName("должен удалять книгу")
    @Test
    void shouldDeleteBook() throws Exception {
        mvc.perform(delete("/api/books/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(bookService).deleteById(new BookIdDto(1L));
    }

    @DisplayName("не должен удалять книгу POST-запросом")
    @Test
    void shouldNotDeleteBookByPost() throws Exception {
        mvc.perform(post("/api/books/1").with(csrf()))
                .andExpect(status().isMethodNotAllowed());

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
