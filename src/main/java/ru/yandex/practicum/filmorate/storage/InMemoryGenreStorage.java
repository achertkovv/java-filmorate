package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.model.Genre.GENRES;

@Component
@Qualifier("inMemoryGenreStorage")
public class InMemoryGenreStorage implements GenreStorage {
    @Override
    public List<Genre> findAll() {
        // возвращаем копию, чтобы вызывающий не мог случайно модифицировать справочник
        return List.copyOf(GENRES);
    }

    @Override
    public Optional<Genre> findById(int id) {
        return GENRES.stream()
                .filter(g -> g.getId() == id)
                .findFirst();
    }
}
