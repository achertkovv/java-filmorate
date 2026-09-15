package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;

import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FilmController.class)
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateFilmWhenDataIsValid() throws Exception {
        String json = "{\n" +
                      "  \"name\": \"Матрица\",\n" +
                      "  \"description\": \"Классика\",\n" +
                      "  \"releaseDate\": \"1999-03-31\",\n" +
                      "  \"duration\": 136\n" +
                      "}\n";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Матрица"));
    }

    @Test
    void shouldReturn400WhenNameIsBlank() throws Exception {
        String json = "{\n" +
                      "  \"name\": \"\",\n" +
                      "  \"description\": \"Классика\",\n" +
                      "  \"releaseDate\": \"1999-03-31\",\n" +
                      "  \"duration\": 136\n" +
                      "}\n";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenDescriptionTooLong() throws Exception {
        String longDescription = "a".repeat(201);
        AtomicReference<String> json = new AtomicReference<>(("{\n" +
                                                              "  \"name\": \"Матрица\",\n" +
                                                              "  \"description\": \"%s\",\n" +
                                                              "  \"releaseDate\": \"1999-03-31\",\n" +
                                                              "  \"duration\": 136\n" +
                                                              "}\n").formatted(longDescription));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json.get()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenReleaseDateBefore1895() throws Exception {
        String json = "{\n" +
                      "  \"name\": \"Старый фильм\",\n" +
                      "  \"description\": \"Очень старый\",\n" +
                      "  \"releaseDate\": \"1895-12-27\",\n" +
                      "  \"duration\": 60\n" +
                      "}\n";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPassWhenReleaseDateIsExactlyMinDate() throws Exception {
        String json = "{\n" +
                      "  \"name\": \"Первый фильм\",\n" +
                      "  \"description\": \"Граница\",\n" +
                      "  \"releaseDate\": \"1895-12-28\",\n" +
                      "  \"duration\": 1\n" +
                      "}\n";

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn400WhenDurationIsZero() throws Exception {
        String json = "{\n" +
                      "  \"name\": \"Матрица\",\n" +
                      "  \"description\": \"Классика\",\n" +
                      "  \"releaseDate\": \"1999-03-31\",\n" +
                      "  \"duration\": 0\n" +
                      "}\n";

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
        String json = "{\n" +
                      "  \"name\": \"Матрица\",\n" +
                      "  \"description\": \"Классика\",\n" +
                      "  \"releaseDate\": \"1999-03-31\",\n" +
                      "  \"duration\": 136\n" +
                      "}\n";

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk())
                .andExpect(content().json("[" + json + "]"));
    }

    @Test
    void shouldReturn404OnUpdateWhenFilmNotFound() throws Exception {
        String json = "{\n" +
                      "  \"id\": 999,\n" +
                      "  \"name\": \"Матрица\",\n" +
                      "  \"description\": \"Классика\",\n" +
                      "  \"releaseDate\": \"1999-03-31\",\n" +
                      "  \"duration\": 136\n" +
                      "}\n";

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}
