package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * User.
 * Используйте аннотацию @Data библиотеки Lombok — с ней будет меньше работы по созданию сущностей.
 */
@Data
public class User {
    private Long id; // целочисленный идентификатор
    private String email; // электронная почта
    private String login; // логин пользователя
    private String name; // имя для отображения
    private LocalDate birthday; // дата рождения

    /** свойство friends в классе пользователя, которое будет содержать список его друзей */
    private final Set<Long> friends = new HashSet<>();
}
