package ru.otus.hw.controllers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Actuator endpoints")
@SpringBootTest
@AutoConfigureMockMvc
class ActuatorEndpointTest {
    @Autowired
    private MockMvc mvc;

    @DisplayName("должны отдавать healthcheck с пользовательским индикатором")
    @Test
    void shouldExposeHealthWithCustomIndicator() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.libraryCatalog.status").value("UP"))
                .andExpect(jsonPath("$.components.libraryCatalog.details.books").value(3))
                .andExpect(jsonPath("$.components.libraryCatalog.details.authors").value(3))
                .andExpect(jsonPath("$.components.libraryCatalog.details.genres").value(3));
    }

    @DisplayName("должны отдавать метрики приложения")
    @Test
    void shouldExposeMetrics() throws Exception {
        mvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    @DisplayName("должны отдавать logfile приложения")
    @Test
    void shouldExposeLogfile() throws Exception {
        mvc.perform(get("/actuator/logfile"))
                .andExpect(status().isOk())
                .andExpect(content().string(notNullValue()))
                .andExpect(content().string(containsString("Started")));
    }
}
