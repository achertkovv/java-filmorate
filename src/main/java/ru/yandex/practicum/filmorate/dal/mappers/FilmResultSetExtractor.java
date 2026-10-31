package ru.yandex.practicum.filmorate.dal.mappers;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FilmResultSetExtractor implements ResultSetExtractor<List<Film>> {

    private final RowMapper<Film> rowMapper = new FilmRowMapper();

    @Override
    public List<Film> extractData(ResultSet rs) throws SQLException {
        Map<Long, Film> films = new LinkedHashMap<>();

        while (rs.next()) {
            long filmId = rs.getLong("id");
            Film film = films.get(filmId);

            if (film == null) {
                film = rowMapper.mapRow(rs, rs.getRow());
                films.put(filmId, film);
            }

            int genreId = rs.getInt("genre_id");
            if (film != null && !rs.wasNull()) {
                film.getGenres().add(new Genre(genreId, rs.getString("genre_name")));
            }
        }

        return new ArrayList<>(films.values());
    }
}
