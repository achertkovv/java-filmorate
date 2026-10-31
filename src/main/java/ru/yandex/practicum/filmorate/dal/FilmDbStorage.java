package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.FilmResultSetExtractor;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film add(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setObject(3, film.getReleaseDate());
            ps.setLong(4, film.getDuration());
            ps.setObject(5, film.getMpa() != null ? film.getMpa().getId() : null);
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKeyAs(Long.class));
        saveGenres(film);
        log.debug("Фильм добавлен в БД: id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, rating_id = ? WHERE id = ?";
        int rows = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null,
                film.getId());

        if (rows == 0) {
            throw new EmptyResultDataAccessException(1);
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, " +
                "m.name AS mpa_name, g.id AS genre_id, g.name AS genre_name " +
                "FROM films f " +
                "LEFT JOIN rating m ON f.rating_id = m.id " +
                "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.id WHERE f.id = ?";
        List<Film> result = jdbcTemplate.query(
                sql,
                new FilmResultSetExtractor(),
                id);
        return result == null ? Optional.empty() : result.stream().findFirst();
    }

    @Override
    public List<Film> findAll() {
        return jdbcTemplate.query(
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, " +
                        "m.name AS mpa_name, g.id AS genre_id, g.name AS genre_name " +
                        "FROM films f " +
                        "LEFT JOIN rating m ON f.rating_id = m.id " +
                        "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                        "LEFT JOIN genres g ON fg.genre_id = g.id ORDER BY f.id",
                new FilmResultSetExtractor());
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Object[]> batch = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .toList();

        jdbcTemplate.batchUpdate(
                "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                batch);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "MERGE INTO user_films (user_id, film_id) KEY (user_id, film_id) VALUES (?, ?)",
                userId, filmId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update(
                "DELETE FROM user_films WHERE user_id = ? AND film_id = ?",
                userId, filmId);
    }

    @Override
    public List<Film> getPopular(int count) {
        String sql =
                "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, " +
                        "m.name AS mpa_name, g.id AS genre_id, g.name AS genre_name " +
                        "FROM ( " +
                        "SELECT f.id, COUNT(uf.user_id) AS likes_count " +
                        "FROM films f " +
                        "LEFT JOIN user_films uf ON f.id = uf.film_id " +
                        "GROUP BY f.id " +
                        "ORDER BY likes_count DESC, f.id " +
                        "LIMIT ? " +
                        ") popular " +
                        "JOIN films f ON f.id = popular.id " +
                        "LEFT JOIN rating m ON f.rating_id = m.id " +
                        "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                        "LEFT JOIN genres g ON fg.genre_id = g.id " +
                        "ORDER BY popular.likes_count DESC, f.id";
        return jdbcTemplate.query(sql, new FilmResultSetExtractor(), count);
    }
}
