package ru.yandex.practicum.filmorate.dal.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));

        Long filmId = film.getId();

        // MPA
        int mpaId = rs.getInt("rating_id");
        if (!rs.wasNull()) {
            film.setMpa(jdbcTemplate.queryForObject(
                    "SELECT id, name FROM rating WHERE id = ?",
                    (r, i) -> new Mpa(r.getInt("id"), r.getString("name")),
                    mpaId));
        }

        // Жанры
        List<Genre> genres = jdbcTemplate.query(
                "SELECT g.id, g.name FROM genres g " +
                        "JOIN film_genres fg ON g.id = fg.genre_id " +
                        "WHERE fg.film_id = ?",
                (r, i) -> new Genre(r.getInt("id"), r.getString("name")),
                filmId);
        film.setGenres(new java.util.LinkedHashSet<>(genres));

        return film;
    }
}
