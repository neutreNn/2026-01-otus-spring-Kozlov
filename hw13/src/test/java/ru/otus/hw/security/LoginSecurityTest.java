package ru.otus.hw.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;

@DisplayName("Аутентификация пользователей")
@SpringBootTest
@AutoConfigureMockMvc
class LoginSecurityTest {
    @Autowired
    private MockMvc mvc;

    @DisplayName("должна выполнять вход для пользователя из БД")
    @Test
    void shouldLoginUserFromDatabase() throws Exception {
        mvc.perform(formLogin().user("reader").password("password"))
                .andExpect(authenticated().withUsername("reader"));
    }
}
