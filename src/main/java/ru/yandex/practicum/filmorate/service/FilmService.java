package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private static final int DEFAULT_POPULAR_LIMIT = 10;

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
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
        if (count <= 0) {
            count = DEFAULT_POPULAR_LIMIT;
        }
        final int limit = count;

        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public List<Film> getPopular() {
        return getPopular(DEFAULT_POPULAR_LIMIT);
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
}
