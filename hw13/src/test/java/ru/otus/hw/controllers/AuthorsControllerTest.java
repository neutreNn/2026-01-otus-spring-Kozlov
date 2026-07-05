package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.models.Author;
import ru.otus.hw.services.AuthorService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Контроллер авторов")
@WebMvcTest(AuthorsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthorsControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private AuthorService authorService;

    @DisplayName("должен отображать список авторов")
    @Test
    void shouldRenderAuthorsList() throws Exception {
        var authors = List.of(new Author(1L, "Author_1"), new Author(2L, "Author_2"));
        when(authorService.findAll()).thenReturn(authors);

        mvc.perform(get("/authors"))
                .andExpect(status().isOk())
                .andExpect(view().name("authors/list"))
                .andExpect(model().attribute("authors", authors))
                .andExpect(content().string(containsString("Author_1")))
                .andExpect(content().string(containsString("Author_2")));
    }
}
