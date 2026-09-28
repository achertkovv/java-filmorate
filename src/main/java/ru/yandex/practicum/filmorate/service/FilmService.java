package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.time.Month;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    // первый в истории публичный платный кинопоказ
    private static final LocalDate MIN_RELEASE_DATE = LocalDate.of(1895, Month.DECEMBER, 28);
    private static final int DEFAULT_POPULAR_LIMIT = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film create(Film film) {
        validateFilm(film);
        log.info("Запрос на добавление фильма: {}", film.getName());
        return filmStorage.add(film);
    }

    public Film update(Film film) {
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
        log.info("Запрос на обновление фильма: id={}", film.getId());
        return filmStorage.update(film);
    }

    public List<Film> getAll() {
        List<Film> films = filmStorage.findAll();
        log.info("Получен список всех фильмов, количество: {}", films.size());
        return films;
    }

    // Пользователь ставит лайк фильму
    public void addLike(Long filmId, Long userId) {
        Film film = getExistingFilm(filmId);
        ensureUserExists(userId);

        film.getLikes().add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    // Удаление лайка. Если пользователь не ставил — ничего не меняется.
    public void removeLike(Long filmId, Long userId) {
        Film film = getExistingFilm(filmId);
        ensureUserExists(userId);

        film.getLikes().remove(userId);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    // Топ фильмов по количеству лайков
    public List<Film> getPopular(int count) {
        // Можно лучше: При count <= 0 значение молча заменяется на 10, и клиент не узнаёт, что передал
        // некорректный параметр. Обычно на такое отвечают 400.
        if (count <= 0) {
            log.warn("Некорректное число фильмов '{}'", count);
            throw new ValidationException("Число фильма должна быть положительным числом");
        }
        final int limit = count;

        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private Film getExistingFilm(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    private void ensureUserExists(Long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    // Логику поиска удобно держать в сервисе, там уже есть похожий приватный метод.
    public Film getById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
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
        if (film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
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
