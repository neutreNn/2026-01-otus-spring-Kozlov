package ru.otus.hw.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Конфигурация web-безопасности")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SecurityConfigTest {
    @Autowired
    private MockMvc mvc;

    @DisplayName("должна перенаправлять анонимного пользователя MVC-страниц на логин")
    @Test
    void shouldRedirectAnonymousMvcRequestsToLogin() throws Exception {
        mvc.perform(get("/books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @DisplayName("должна возвращать 401 для анонимного REST-запроса")
    @Test
    void shouldReturnUnauthorizedForAnonymousApiRequests() throws Exception {
        mvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("должна запрещать форму создания книги обычному пользователю")
    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectBookCreatePageForRegularUser() throws Exception {
        mvc.perform(get("/books/new"))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должна разрешать форму создания книги редактору")
    @Test
    @WithMockUser(roles = "EDITOR")
    void shouldAllowBookCreatePageForEditor() throws Exception {
        mvc.perform(get("/books/new"))
                .andExpect(status().isOk());
    }

    @DisplayName("должна запрещать создание книги через MVC POST")
    @Test
    @WithMockUser(roles = "EDITOR")
    void shouldRejectUnsupportedMvcBookPost() throws Exception {
        mvc.perform(post("/books").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должна запрещать создание книги через REST обычному пользователю")
    @Test
    @WithMockUser(roles = "USER")
    void shouldRejectBookCreateApiForRegularUser() throws Exception {
        mvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "New Book",
                                  "authorId": 1,
                                  "genreId": 1
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должна разрешать создание книги через REST редактору")
    @Test
    @WithMockUser(roles = "EDITOR")
    void shouldAllowBookCreateApiForEditor() throws Exception {
        mvc.perform(post("/api/books")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Editor Book",
                                  "authorId": 1,
                                  "genreId": 1
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @DisplayName("должна запрещать удаление книги через REST редактору")
    @Test
    @WithMockUser(roles = "EDITOR")
    void shouldRejectBookDeleteApiForEditor() throws Exception {
        mvc.perform(delete("/api/books/1").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @DisplayName("должна разрешать удаление книги через REST администратору")
    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowBookDeleteApiForAdmin() throws Exception {
        mvc.perform(delete("/api/books/1").with(csrf()))
                .andExpect(status().isNoContent());
    }
}
