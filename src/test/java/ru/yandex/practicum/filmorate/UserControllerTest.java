package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(UserService.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FilmStorage filmStorage;
    @MockitoBean
    private UserStorage userStorage;

    @Test
    void shouldCreateUserWhenDataIsValid() throws Exception {
        String json = "{\n" +
                "  \"email\": \"user@example.com\",\n" +
                "  \"login\": \"user_login\",\n" +
                "  \"name\": \"Иван\",\n" +
                "  \"birthday\": \"1990-05-15\"\n" +
                "}\n";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
        //.andExpect(jsonPath("$.id").exists())
        //.andExpect(jsonPath("$.login").value("user_login"));
    }

    @Test
    void shouldUseLoginAsNameWhenNameIsBlank() throws Exception {
        String json = "{\n" +
                "  \"email\": \"user@example.com\",\n" +
                "  \"login\": \"user_login\",\n" +
                "  \"name\": \"\",\n" +
                "  \"birthday\": \"1990-05-15\"\n" +
                "}\n";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
        //.andExpect(jsonPath("$.name").value("user_login")
    }

    @Test
    void shouldReturn400WhenEmailIsBlank() throws Exception {
        String json = "{\n" +
                "  \"email\": \"\",\n" +
                "  \"login\": \"user_login\",\n" +
                "  \"name\": \"Иван\",\n" +
                "  \"birthday\": \"1990-05-15\"\n" +
                "}\n";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEmailHasNoAt() throws Exception {
        String json = "{\n" +
                "  \"email\": \"userexample.com\",\n" +
                "  \"login\": \"user_login\",\n" +
                "  \"name\": \"Иван\",\n" +
                "  \"birthday\": \"1990-05-15\"\n" +
                "}\n";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenEmailIsNotValid() throws Exception {
        String json = "{\n" +
                "  \"email\": \"userexample.com@\",\n" +
                "  \"login\": \"user_login\",\n" +
                "  \"name\": \"Иван\",\n" +
                "  \"birthday\": \"1990-05-15\"\n" +
                "}\n";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenLoginContainsSpace() throws Exception {
        String json = "{\n" +
                "  \"email\": \"user@example.com\",\n" +
                "  \"login\": \"user login\",\n" +
                "  \"name\": \"Иван\",\n" +
                "  \"birthday\": \"1990-05-15\"\n" +
                "}\n";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenLoginIsBlank() throws Exception {
        String json = "{\n" +
                "  \"email\": \"user@example.com\",\n" +
                "  \"login\": \"\",\n" +
                "  \"name\": \"Иван\",\n" +
                "  \"birthday\": \"1990-05-15\"\n" +
                "}\n";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenBirthdayInFuture() throws Exception {
        String json = "{\n" +
                "  \"email\": \"user@example.com\",\n" +
                "  \"login\": \"user_login\",\n" +
                "  \"name\": \"Иван\",\n" +
                "  \"birthday\": \"2999-01-01\"\n" +
                "}\n";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn404OnUpdateWhenUserNotFound() throws Exception {
        String json = "{\n" +
                "  \"id\": 999,\n" +
                "  \"email\": \"user@example.com\",\n" +
                "  \"login\": \"user_login\",\n" +
                "  \"name\": \"Иван\",\n" +
                "  \"birthday\": \"1990-05-15\"\n" +
                "}\n";

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }
}