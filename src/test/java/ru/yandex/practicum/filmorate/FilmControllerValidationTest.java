package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerValidationTest {

    private FilmController controller;

    @BeforeEach
    void setup() {
        controller = new FilmController();
    }

    /**
     * Валидный фильм — от него будем "портить" одно поле.
     */
    private Film validFilm() {
        Film film = new Film();
        film.setName("Матрица");
        film.setDescription("Классика");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136L);
        return film;
    }

    // ---------- Общий случай ----------

    @Test
    void shouldAddValidFilm() {
        Film result = controller.addFilm(validFilm());
        assertNotNull(result.getId(), "id должен быть присвоен");
        assertEquals("Матрица", result.getName());
    }

    @Test
    void shouldThrowWhenFilmIsNull() {
        assertThrows(ValidationException.class, () -> controller.addFilm(null));
    }

    // ---------- name ----------

    @Test
    void shouldRejectWhenNameIsNull() {
        Film film = validFilm();
        film.setName(null);
        assertThrows(ConditionsNotMetException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldRejectWhenNameIsEmpty() {
        Film film = validFilm();
        film.setName("");
        assertThrows(ConditionsNotMetException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldRejectWhenNameIsBlank() {
        Film film = validFilm();
        film.setName("   ");
        assertThrows(ConditionsNotMetException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldAcceptSingleCharName() {
        Film film = validFilm();
        film.setName("A");
        assertDoesNotThrow(() -> controller.addFilm(film));
    }

    // ---------- description ----------

    @Test
    void shouldAcceptNullDescription() {
        Film film = validFilm();
        film.setDescription(null);
        assertDoesNotThrow(() -> controller.addFilm(film));
    }

    @Test
    void shouldAcceptEmptyDescription() {
        Film film = validFilm();
        film.setDescription("");
        assertDoesNotThrow(() -> controller.addFilm(film));
    }

    @Test
    void shouldAcceptDescriptionWith200Chars() {
        Film film = validFilm();
        film.setDescription("a".repeat(200));
        assertDoesNotThrow(() -> controller.addFilm(film),
                "200 символов — ровно на границе, валидно");
    }

    @Test
    void shouldRejectDescriptionWith201Chars() {
        Film film = validFilm();
        film.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> controller.addFilm(film),
                "201 символ — на 1 больше лимита");
    }

    // ---------- releaseDate ----------

    @Test
    void shouldRejectWhenReleaseDateIsNull() {
        Film film = validFilm();
        film.setReleaseDate(null);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldAcceptWhenReleaseDateIsExactly1895_12_28() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        assertDoesNotThrow(() -> controller.addFilm(film),
                "28.12.1895 — самая ранняя допустимая дата");
    }

    @Test
    void shouldRejectWhenReleaseDateIs1895_12_27() {
        Film film = validFilm();
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> controller.addFilm(film),
                "27.12.1895 — на 1 день раньше лимита");
    }

    // ---------- duration ----------

    @Test
    void shouldRejectWhenDurationIsNull() {
        Film film = validFilm();
        film.setDuration(null);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldRejectWhenDurationIsZero() {
        Film film = validFilm();
        film.setDuration(0L);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldRejectWhenDurationIsNegative() {
        Film film = validFilm();
        film.setDuration((long) -1);
        assertThrows(ValidationException.class, () -> controller.addFilm(film));
    }

    @Test
    void shouldAcceptWhenDurationIsOne() {
        Film film = validFilm();
        film.setDuration(1L);
        assertDoesNotThrow(() -> controller.addFilm(film),
                "Продолжительность 1 — минимальная валидная");
    }

    // ---------- update ----------

    @Test
    void shouldRejectUpdateWhenIdIsNull() {
        Film film = validFilm();
        // id не задаём
        assertThrows(ConditionsNotMetException.class, () -> controller.updateFilm(film));
    }

    @Test
    void shouldRejectUpdateWhenFilmNotFound() {
        Film film = validFilm();
        film.setId(999L);
        assertThrows(NotFoundException.class, () -> controller.updateFilm(film));
    }
}