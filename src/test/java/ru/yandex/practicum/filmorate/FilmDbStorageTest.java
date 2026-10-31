package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.FilmDbStorage;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    private Film newFilm(String name, int mpaId, int... genreIds) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136L);
        if (mpaId > 0) {
            film.setMpa(new Mpa(mpaId, null));
        }
        Set<Genre> genres = new LinkedHashSet<>();
        for (int id : genreIds) {
            genres.add(new Genre(id, null));
        }
        film.setGenres(genres);
        return film;
    }

    private User newUser(String login) {
        User user = new User();
        user.setEmail(login + "@b.c");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void addShouldPersistFilmAndAssignId() {
        Film saved = filmStorage.add(newFilm("Матрица", 4, 4, 6));

        assertThat(saved.getId()).isNotNull();

        assertThat(filmStorage.findById(saved.getId()))
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getName()).isEqualTo("Матрица");
                    assertThat(f.getDescription()).isEqualTo("Описание");
                    assertThat(f.getReleaseDate()).isEqualTo(LocalDate.of(1999, 3, 31));
                    assertThat(f.getDuration()).isEqualTo(136);
                });
    }

    @Test
    void addShouldResolveMpaAndGenres() {
        Film saved = filmStorage.add(newFilm("Матрица", 4, 4, 6));

        Film found = filmStorage.findById(saved.getId()).orElseThrow();

        assertThat(found.getMpa()).isNotNull();
        assertThat(found.getMpa().getId()).isEqualTo(4);
        assertThat(found.getMpa().getName()).isEqualTo("R");

        assertThat(found.getGenres())
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Триллер", "Боевик");
    }

    @Test
    void addWithoutMpaAndGenresShouldWork() {
        Film saved = filmStorage.add(newFilm("Без рейтинга", 0));

        Film found = filmStorage.findById(saved.getId()).orElseThrow();

        assertThat(found.getMpa()).isNull();
        assertThat(found.getGenres()).isEmpty();
    }

    @Test
    void findByIdShouldReturnEmptyForUnknownId() {
        assertThat(filmStorage.findById(999L)).isEmpty();
    }

    @Test
    void findAllShouldReturnAllFilms() {
        filmStorage.add(newFilm("Матрица", 4));
        filmStorage.add(newFilm("Терминатор", 4));
        filmStorage.add(newFilm("Начало", 3));

        assertThat(filmStorage.findAll())
                .extracting(Film::getName)
                .containsExactlyInAnyOrder("Матрица", "Терминатор", "Начало");
    }

    @Test
    void updateShouldModifyFilm() {
        Film saved = filmStorage.add(newFilm("Матрица", 4, 4));

        saved.setName("Матрица: Ремастер");
        saved.setDuration(140L);
        saved.setMpa(new Mpa(5, null));
        saved.setGenres(new LinkedHashSet<>(Set.of(new Genre(1, null))));
        filmStorage.update(saved);

        Film found = filmStorage.findById(saved.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("Матрица: Ремастер");
        assertThat(found.getDuration()).isEqualTo(140);
        assertThat(found.getMpa().getName()).isEqualTo("NC-17");
        assertThat(found.getGenres())
                .extracting(Genre::getName)
                .containsExactly("Комедия");
    }

    @Test
    void addLikeShouldPersistLike() {
        Film film = filmStorage.add(newFilm("Матрица", 4));
        User user = userStorage.add(newUser("alice"));

        filmStorage.addLike(film.getId(), user.getId());

        assertThat(filmStorage.getPopular(10))
                .extracting(Film::getId)
                .contains(film.getId());
    }

    @Test
    void addLikeTwiceShouldNotDuplicate() {
        Film film = filmStorage.add(newFilm("Матрица", 4));
        User user = userStorage.add(newUser("alice"));

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.addLike(film.getId(), user.getId());   // повторный MERGE

        // не падает, лайк один
        assertThat(filmStorage.getPopular(10)).hasSize(1);
    }

    @Test
    void removeLikeShouldDeleteLike() {
        Film film = filmStorage.add(newFilm("Матрица", 4));
        User user = userStorage.add(newUser("alice"));

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        // лайка нет, но фильм остался
        assertThat(filmStorage.findById(film.getId())).isPresent();
    }

    @Test
    void getPopularShouldSortByLikesDesc() {
        Film a = filmStorage.add(newFilm("Первый", 4));
        Film b = filmStorage.add(newFilm("Второй", 4));
        Film c = filmStorage.add(newFilm("Третий", 4));

        User u1 = userStorage.add(newUser("u1"));
        User u2 = userStorage.add(newUser("u2"));

        filmStorage.addLike(a.getId(), u1.getId());
        filmStorage.addLike(c.getId(), u1.getId());
        filmStorage.addLike(c.getId(), u2.getId());

        List<Film> popular = filmStorage.getPopular(10);

        assertThat(popular)
                .extracting(Film::getId)
                .containsExactly(c.getId(), a.getId(), b.getId());
    }

    @Test
    void getPopularShouldRespectLimit() {
        for (int i = 0; i < 5; i++) {
            filmStorage.add(newFilm("Фильм " + i, 4));
        }

        assertThat(filmStorage.getPopular(3)).hasSize(3);
        assertThat(filmStorage.getPopular(100)).hasSize(5);
    }
}
