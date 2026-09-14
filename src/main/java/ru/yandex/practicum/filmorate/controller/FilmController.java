package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    // Для хранения данных используйте HashMap.
    private final Map<Long, Film> films = new HashMap<>();

    // первый в истории публичный платный кинопоказ
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, Month.DECEMBER, 28);

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен список всех фильмов, количество: {}", films.size());
        return films.values();
    }

    // Используйте аннотацию @RequestBody, чтобы создать объект из тела запроса на добавление или обновление сущности.
    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        validateFilmIsNull(film);
        log.info("Запрос на добавление фильма: {}", film.getName());
        // название не может быть пустым;
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ConditionsNotMetException("Название не может быть пустым");
        }
        validateDescription(film);
        validateDuration(film);
        validateReleaseDate(film);
        // формируем дополнительные данные
        film.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        validateFilmIsNull(newFilm);
        log.info("Запрос на обновление фильма: id={}", newFilm.getId());
        // Если при изменении данных пользователя не указан его идентификатор, то должно генерироваться
        // исключение ConditionsNotMetException с текстом: "Id должен быть указан".
        if (newFilm.getId() == null) {
            log.warn("ID фильма '{}' не задан", newFilm.getName());
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            // Если название пустое - то ничего не меняем
            if (newFilm.getName() == null) {
                return oldFilm;
            }
            validateDescription(newFilm);
            validateDuration(newFilm);
            validateReleaseDate(newFilm);
            oldFilm.setName(newFilm.getName());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setDuration(newFilm.getDuration());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            log.info("Обновлён фильм: {}", oldFilm);
            return oldFilm;
        }
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    private void validateFilmIsNull(Film film) {
        if (film == null) {
            log.warn("Попытка работать с фильмом null");
            throw new ValidationException("Фильм не может быть nul");
        }
    }

    private void validateReleaseDate(Film film) {
        if (film.getReleaseDate() == null) {
            log.warn("Пустая дата релиза фильма");
            throw new ValidationException("Дата релиза обязательна");
        }
        // дата релиза — не раньше 28 декабря 1895 года;
        if (film.getReleaseDate().isBefore(MIN_RELEASE_DATE)) {
            log.warn("Некорректная дата релиза фильма '{}': {}", film.getName(), film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }

    private void validateDuration(Film film) {
        if (film.getDuration() == null) {
            log.warn("Пустая продолжительность фильма");
        }
        // продолжительность фильма должна быть положительным числом.
        if (film.getDuration() <= 0) {
            log.warn("Некорректная продолжительность фильма '{}': {}", film.getName(), film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private void validateDescription(Film film) {
        if (film.getDescription() == null) {
            log.warn("Пустое описание фильма");
        }
        // максимальная длина описания — 200 символов;
        if (film.getDescription().length() > 200) {
            log.warn("Некорректная длина описания фильма '{}': {}", film.getName(), film.getDescription().length());
            throw new ValidationException("Длина описания должна содержать менее 200 символов");
        }
    }

    // Метод getNextId для генерации идентификатора пользователя при создании аккаунта
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
