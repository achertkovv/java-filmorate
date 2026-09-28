package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {
    Film add(Film film);
    Film update(Film film);
    Optional<Film> findById(Long id);
    List<Film> findAll();
}
