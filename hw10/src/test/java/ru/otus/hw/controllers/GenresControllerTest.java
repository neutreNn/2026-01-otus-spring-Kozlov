package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.otus.hw.models.Genre;
import ru.otus.hw.services.GenreService;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@DisplayName("Контроллер жанров")
@WebMvcTest(GenresController.class)
class GenresControllerTest {
    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private GenreService genreService;

    @DisplayName("должен отображать список жанров")
    @Test
    void shouldRenderGenresList() throws Exception {
        var genres = List.of(new Genre(1L, "Genre_1"), new Genre(2L, "Genre_2"));
        when(genreService.findAll()).thenReturn(genres);

        mvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("genres/list"))
                .andExpect(model().attribute("genres", genres))
                .andExpect(content().string(containsString("Genre_1")))
                .andExpect(content().string(containsString("Genre_2")));
    }
}
