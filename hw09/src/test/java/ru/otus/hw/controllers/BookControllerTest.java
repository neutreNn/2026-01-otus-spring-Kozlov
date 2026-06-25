package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.BookComment;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.AuthorService;
import ru.otus.hw.services.BookCommentService;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.GenreService;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Контроллер книг")
@WebMvcTest(BookController.class)
class BookControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private AuthorService authorService;

    @MockitoBean
    private GenreService genreService;

    @MockitoBean
    private BookCommentService bookCommentService;

    @DisplayName("должен перенаправлять с главной страницы на список книг")
    @Test
    void shouldRedirectRootToBooksList() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    @DisplayName("должен отображать список книг")
    @Test
    void shouldRenderBooksList() throws Exception {
        var books = List.of(book(1L, "BookTitle_1", author(1L), genre(1L)));
        when(bookService.findAll()).thenReturn(books);

        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(model().attribute("books", books))
                .andExpect(content().string(containsString("BookTitle_1")))
                .andExpect(content().string(containsString("Author_1")))
                .andExpect(content().string(containsString("Genre_1")));
    }

    @DisplayName("должен отображать книгу с комментариями")
    @Test
    void shouldRenderBookDetailsWithComments() throws Exception {
        var book = book(1L, "BookTitle_1", author(1L), genre(1L));
        var comments = List.of(new BookComment(1L, "Comment_1", book));
        when(bookService.findById(1L)).thenReturn(Optional.of(book));
        when(bookCommentService.findByBookId(1L)).thenReturn(comments);

        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/details"))
                .andExpect(model().attribute("book", book))
                .andExpect(model().attribute("comments", comments))
                .andExpect(content().string(containsString("BookTitle_1")))
                .andExpect(content().string(containsString("Comment_1")));
    }

    @DisplayName("должен возвращать 404 для отсутствующей книги")
    @Test
    void shouldReturnNotFoundForMissingBook() throws Exception {
        when(bookService.findById(404L)).thenReturn(Optional.empty());

        mvc.perform(get("/books/404"))
                .andExpect(status().isNotFound());
    }

    @DisplayName("должен отображать форму создания книги")
    @Test
    void shouldRenderNewBookForm() throws Exception {
        mockReferenceData();

        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("bookForm", "authors", "genres", "formAction"))
                .andExpect(model().attribute("formAction", "/books"))
                .andExpect(content().string(containsString("Author_1")))
                .andExpect(content().string(containsString("Genre_1")));
    }

    @DisplayName("должен создавать книгу и перенаправлять на страницу просмотра")
    @Test
    void shouldCreateBook() throws Exception {
        var savedBook = book(4L, "New Book", author(1L), genre(2L));
        when(bookService.insert("New Book", 1L, 2L)).thenReturn(savedBook);

        mvc.perform(post("/books")
                        .param("title", "  New Book  ")
                        .param("authorId", "1")
                        .param("genreId", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/4"));

        verify(bookService).insert("New Book", 1L, 2L);
    }

    @DisplayName("должен возвращать форму создания при ошибках валидации")
    @Test
    void shouldReturnCreateFormOnValidationErrors() throws Exception {
        mockReferenceData();

        mvc.perform(post("/books")
                        .param("title", " ")
                        .param("authorId", "0")
                        .param("genreId", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasFieldErrors("bookForm", "title", "authorId", "genreId"))
                .andExpect(model().attribute("formAction", "/books"));
    }

    @DisplayName("должен отображать форму редактирования книги")
    @Test
    void shouldRenderEditBookForm() throws Exception {
        var book = book(1L, "BookTitle_1", author(1L), genre(1L));
        when(bookService.findById(1L)).thenReturn(Optional.of(book));
        mockReferenceData();

        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeExists("bookForm", "authors", "genres", "formAction"))
                .andExpect(model().attribute("formAction", "/books/1"))
                .andExpect(content().string(containsString("BookTitle_1")));
    }

    @DisplayName("должен обновлять книгу и перенаправлять на страницу просмотра")
    @Test
    void shouldUpdateBook() throws Exception {
        mvc.perform(post("/books/1")
                        .param("title", "  Updated Book  ")
                        .param("authorId", "2")
                        .param("genreId", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/1"));

        verify(bookService).update(1L, "Updated Book", 2L, 3L);
    }

    @DisplayName("должен возвращать форму редактирования при ошибках валидации")
    @Test
    void shouldReturnEditFormOnValidationErrors() throws Exception {
        mockReferenceData();

        mvc.perform(post("/books/1")
                        .param("title", "")
                        .param("authorId", "0")
                        .param("genreId", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attributeHasFieldErrors("bookForm", "title", "authorId", "genreId"))
                .andExpect(model().attribute("formAction", "/books/1"));
    }

    @DisplayName("должен удалять книгу только POST-запросом")
    @Test
    void shouldDeleteBookByPost() throws Exception {
        mvc.perform(post("/books/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));

        verify(bookService).deleteById(1L);
    }

    @DisplayName("не должен удалять книгу GET-запросом")
    @Test
    void shouldNotDeleteBookByGet() throws Exception {
        mvc.perform(get("/books/1/delete"))
                .andExpect(status().isMethodNotAllowed());

        verifyNoInteractions(bookService);
    }

    private void mockReferenceData() {
        when(authorService.findAll()).thenReturn(List.of(author(1L), author(2L)));
        when(genreService.findAll()).thenReturn(List.of(genre(1L), genre(2L)));
    }

    private static Book book(long id, String title, Author author, Genre genre) {
        return new Book(id, title, author, genre);
    }

    private static Author author(long id) {
        return new Author(id, "Author_%d".formatted(id));
    }

    private static Genre genre(long id) {
        return new Genre(id, "Genre_%d".formatted(id));
    }
}
