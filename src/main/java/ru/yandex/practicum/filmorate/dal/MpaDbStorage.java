package ru.yandex.practicum.filmorate.dal;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("mpaDbStorage")
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name FROM rating ORDER BY id",
                (rs, i) -> new Mpa(rs.getInt("id"), rs.getString("name")));
    }

    @Override
    public Optional<Mpa> findById(int id) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                    "SELECT id, name FROM rating WHERE id = ?",
                    (rs, i) -> new Mpa(rs.getInt("id"), rs.getString("name")),
                    id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
