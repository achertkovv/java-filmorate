package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mpa {
    private int id;
    private String name;

    public static final List<Mpa> MPA = List.of(
            new Mpa(1, "G"),
            new Mpa(2, "PG"),
            new Mpa(3, "PG-13"),
            new Mpa(4, "R"),
            new Mpa(5, "NC-17")
    );

    public static Mpa of(int id) {
        return MPA.stream()
                .filter(m -> m.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный id рейтинга: " + id));
    }
}
