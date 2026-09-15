package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateFilmWhenDataIsValid() throws Exception {
        String json = """
                {
                  "name": "Матрица",
                  "description": "Классика",
                  "releaseDate": "1999-03-31",
                  "duration": 136
                }
                """;

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Матрица"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        String json = """
                {
                  "name": "",
                  "description": "Классика",
                  "releaseDate": "1999-03-31",
                  "duration": 136
                }
                """;

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenDescriptionTooLong() throws Exception {
        String longDescription = "a".repeat(201);
        String json = """
                {
                  "name": "Матрица",
                  "description": "%s",
                  "releaseDate": "1999-03-31",
                  "duration": 136
                }
                """.formatted(longDescription);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenReleaseDateBefore1895() throws Exception {
        String json = """
                {
                  "name": "Старый фильм",
                  "description": "Очень старый",
                  "releaseDate": "1895-12-27",
                  "duration": 60
                }
                """;

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPassWhenReleaseDateIsExactlyMinDate() throws Exception {
        String json = """
                {
                  "name": "Первый фильм",
                  "description": "Граница",
                  "releaseDate": "1895-12-28",
                  "duration": 1
                }
                """;

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenDurationIsZero() throws Exception {
        String json = """
                {
                  "name": "Матрица",
                  "description": "Классика",
                  "releaseDate": "1999-03-31",
                  "duration": 0
                }
                """;

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenJsonIsMalformed() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnAllFilms() throws Exception {
        String json = """
                {
                  "name": "Матрица",
                  "description": "Классика",
                  "releaseDate": "1999-03-31",
                  "duration": 136
                }
                """;

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json("[" + json + "]"));
    }

    @Test
    void shouldReturn404OnUpdateWhenUserNotFound() throws Exception {
        String json = """
                {
                  "id": 999,
                  "name": "Матрица",
                  "description": "Классика",
                  "releaseDate": "1999-03-31",
                  "duration": 136
                }
                """;

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}
