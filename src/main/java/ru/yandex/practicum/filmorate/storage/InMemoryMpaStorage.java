package ru.yandex.practicum.filmorate.storage;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static ru.yandex.practicum.filmorate.model.Mpa.MPA;

@Component
@Qualifier("inMemoryMpaStorage")
public class InMemoryMpaStorage implements MpaStorage {
    @Override
    public List<Mpa> findAll() {
        return List.copyOf(MPA);
    }

    @Override
    public Optional<Mpa> findById(int id) {
        return MPA.stream()
                .filter(m -> m.getId() == id)
                .findFirst();
    }
}