package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Test
    void findAllShouldReturnSixGenresSortedById() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres)
                .hasSize(6)
                .extracting(Genre::getId)
                .containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void findAllShouldContainExpectedNames() {
        assertThat(genreStorage.findAll())
                .extracting(Genre::getName)
                .containsExactly(
                        "Комедия", "Драма", "Мультфильм",
                        "Триллер", "Документальный", "Боевик");
    }

    @Test
    void findByIdShouldReturnGenre() {
        Optional<Genre> result = genreStorage.findById(3);

        assertThat(result)
                .isPresent()
                .hasValueSatisfying(g -> {
                    assertThat(g.getId()).isEqualTo(3);
                    assertThat(g.getName()).isEqualTo("Мультфильм");
                });
    }

    @Test
    void findByIdShouldReturnEmptyForUnknownId() {
        assertThat(genreStorage.findById(999)).isEmpty();
    }
}