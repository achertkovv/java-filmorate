package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    // Для хранения данных используйте HashMap.
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film add(Film film) {
        // формируем дополнительные данные
        film.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @Override
    public Film update(Film film) {
        Film oldFilm = films.get(film.getId());
        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setDuration(film.getDuration());
        oldFilm.setReleaseDate(film.getReleaseDate());
        log.debug("Фильм обновлён в хранилище: id={}", film.getId());
        return oldFilm;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
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
