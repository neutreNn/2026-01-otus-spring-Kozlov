package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Защита контроллеров Spring Security")
@SpringBootTest
@AutoConfigureMockMvc
class ControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @DisplayName("должна перенаправлять неаутентифицированного пользователя на страницу логина")
    @ParameterizedTest
    @ValueSource(strings = {
            "/",
            "/books",
            "/books/1",
            "/books/new",
            "/books/1/edit",
            "/authors",
            "/genres",
            "/api/books",
            "/api/books/1",
            "/api/books/1/comments",
            "/api/authors",
            "/api/genres"
    })
    void shouldRedirectAnonymousUserToLogin(String url) throws Exception {
        mvc.perform(get(url))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @DisplayName("должна открывать страницу логина всем пользователям")
    @Test
    void shouldAllowAnonymousUserToOpenLoginPage() throws Exception {
        mvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("name=\"_csrf\"")))
                .andExpect(unauthenticated());
    }

    @DisplayName("должна аутентифицировать пользователя из БД")
    @Test
    void shouldAuthenticateUserFromDatabase() throws Exception {
        mvc.perform(formLogin().user("user").password("password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(authenticated().withUsername("user"));
    }
}
