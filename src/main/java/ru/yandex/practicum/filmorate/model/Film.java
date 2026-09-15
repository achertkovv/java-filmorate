package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

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
}
