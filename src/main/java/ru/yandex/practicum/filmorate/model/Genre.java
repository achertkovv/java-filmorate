package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Genre {
    private int id;
    private String name;

    /**
     * Справочник доступных жанров — аналог "статических" данных.
     */
    public static final List<Genre> GENRES = List.of(
            new Genre(1, "Комедия"),
            new Genre(2, "Драма"),
            new Genre(3, "Мультфильм"),
            new Genre(4, "Триллер"),
            new Genre(5, "Документальный"),
            new Genre(6, "Боевик")
    );

    public static Genre of(int id) {
        return GENRES.stream()
                .filter(g -> g.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный id жанра: " + id));
    }
}
