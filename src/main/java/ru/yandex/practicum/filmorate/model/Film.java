package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 * Используйте аннотацию @Data библиотеки Lombok — с ней будет меньше работы по созданию сущностей.
 */
@Data
public class Film {
    private Long id; // целочисленный идентификатор
    private String name; // название
    private String description; // описание
    private LocalDate releaseDate; // дата релиза
    private Long duration; // продолжительность фильма

    /**
     * Несколько жанров. HashSet — чтобы один жанр не дублировался.
     */
    private Set<Genre> genres = new HashSet<>();

    /**
     * Рейтинг MPA — один на фильм.
     */
    private Mpa mpa;

    /**
     * id пользователей, поставивших лайк - один пользователь — один лайк
     */
    private final Set<Long> likes = new HashSet<>();
}
