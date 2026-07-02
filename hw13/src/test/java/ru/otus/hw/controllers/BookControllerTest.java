package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.config.SecurityConfig;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("MVC-контроллер страниц книг")
@WebMvcTest(BookController.class)
@Import(SecurityConfig.class)
class BookControllerTest {
    @Autowired
    private MockMvc mvc;

    @DisplayName("должен перенаправлять с главной страницы на список книг")
    @Test
    @WithMockUser
    void shouldRedirectRootToBooksList() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    @DisplayName("должен отображать страницу списка книг")
    @Test
    @WithMockUser
    void shouldRenderBooksListPage() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/list"))
                .andExpect(content().string(containsString("/js/books-list.js")))
                .andExpect(content().string(containsString("data-books-table")));
    }

    @DisplayName("должен отображать страницу книги с идентификатором для AJAX-загрузки")
    @Test
    @WithMockUser
    void shouldRenderBookDetailsPage() throws Exception {
        mvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/details"))
                .andExpect(model().attribute("bookId", 1L))
                .andExpect(content().string(containsString("/js/book-details.js")))
                .andExpect(content().string(containsString("data-book-id=\"1\"")));
    }

    @DisplayName("должен отображать страницу создания книги")
    @Test
    @WithMockUser(roles = "EDITOR")
    void shouldRenderNewBookPage() throws Exception {
        mvc.perform(get("/books/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(content().string(containsString("/js/book-form.js")))
                .andExpect(content().string(containsString("Новая книга")));
    }

    @DisplayName("должен отображать страницу редактирования книги с идентификатором для AJAX-загрузки")
    @Test
    @WithMockUser(roles = "EDITOR")
    void shouldRenderEditBookPage() throws Exception {
        mvc.perform(get("/books/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/form"))
                .andExpect(model().attribute("bookId", 1L))
                .andExpect(content().string(containsString("data-book-id=\"1\"")))
                .andExpect(content().string(containsString("Книга #1")));
    }

    @DisplayName("не должен выполнять создание книги через MVC POST")
    @Test
    @WithMockUser(roles = "EDITOR")
    void shouldNotCreateBookByMvcPost() throws Exception {
        mvc.perform(post("/books").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должен требовать аутентификацию для списка книг")
    @Test
    void shouldRequireAuthenticationForBooksListPage() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @DisplayName("не должен открывать форму создания книги обычному пользователю")
    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectBookCreatePageForRegularUser() throws Exception {
        mvc.perform(get("/books/new"))
                .andExpect(status().isForbidden());
    }
}
