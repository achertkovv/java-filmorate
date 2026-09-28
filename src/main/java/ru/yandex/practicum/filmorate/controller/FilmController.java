package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    // первый в истории публичный платный кинопоказ
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, Month.DECEMBER, 28);

    private final FilmStorage filmStorage;
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmStorage filmStorage, FilmService filmService) {
        this.filmStorage = filmStorage;
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        List<Film> films = filmStorage.findAll();
        log.info("Получен список всех фильмов, количество: {}", films.size());
        return films;
    }

    // Используйте аннотацию @RequestBody, чтобы создать объект из тела запроса на добавление или обновление сущности.
    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("Запрос на добавление фильма: {}", film != null ? film.getName() : "null");
        validateFilm(film);
        return filmStorage.add(film);
    }

    // Используйте аннотацию @RequestBody, чтобы создать объект из тела запроса на добавление или обновление сущности.
    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("Запрос на обновление фильма: id={}", film != null ? film.getName() : "null");
        validateFilm(film);
        // Если при изменении данных пользователя не указан его идентификатор, то должно генерироваться
        // исключение ConditionsNotMetException с текстом: "Id должен быть указан".
        if (film.getId() == null) {
            log.warn("ID фильма '{}' не задан", film.getName());
            throw new ConditionsNotMetException("ID должен быть указан");
        }
        if (filmStorage.findById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }
        return filmStorage.update(film);
    }

    // PUT /films/{id}/like/{userId} — пользователь ставит лайк фильму.
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    // DELETE /films/{id}/like/{userId} — пользователь удаляет лайк.
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    // GET /films/popular?count={count} — возвращает список из первых count фильмов по количеству лайков.
    // Если значение параметра count не задано, верните первые 10
    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }

    private void validateFilm(Film film) {
        if (film == null) {
            log.warn("Попытка работать с фильмом null");
            throw new ValidationException("Фильм не может быть null");
        }
        if (film.getReleaseDate() == null) {
            log.warn("Пустая дата релиза фильма");
            throw new ValidationException("Дата релиза обязательна");
        }
        // дата релиза — не раньше 28 декабря 1895 года;
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Некорректная дата релиза фильма '{}': {}", film.getName(), film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
        if (film.getDuration() == null) {
            log.warn("Пустая продолжительность фильма");
            throw new ValidationException("Продолжительность фильма обязательна");
        }
        // продолжительность фильма должна быть положительным числом.
        if (film.getDuration() <= 0) {
            log.warn("Некорректная продолжительность фильма '{}': {}", film.getName(), film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getDescription() == null) {
            log.warn("Пустое описание фильма");
            return;
        }
        // максимальная длина описания — 200 символов;
        if (film.getDescription().length() > 200) {
            log.warn("Некорректная длина описания фильма '{}': {}", film.getName(), film.getDescription().length());
            throw new ValidationException("Длина описания должна содержать менее 200 символов");
        }
        // название не может быть пустым;
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Название фильма не задано");
            throw new ConditionsNotMetException("Название не может быть пустым");
        }
    }
}
