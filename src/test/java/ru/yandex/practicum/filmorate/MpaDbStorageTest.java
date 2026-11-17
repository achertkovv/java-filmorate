package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.MpaDbStorage;
import ru.yandex.practicum.filmorate.model.Mpa;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void findAllShouldReturnFiveMpaSortedById() {
        assertThat(mpaStorage.findAll())
                .hasSize(5)
                .extracting(Mpa::getId)
                .containsExactlyInAnyOrder(1, 2, 3, 4, 5);
    }

    @Test
    void findAllShouldMatchAmericanSystem() {
        assertThat(mpaStorage.findAll())
                .extracting(Mpa::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void findByIdShouldReturnMpa() {
        assertThat(mpaStorage.findById(4))
                .isPresent()
                .hasValueSatisfying(m -> {
                    assertThat(m.getId()).isEqualTo(4);
                    assertThat(m.getName()).isEqualTo("R");
                });
    }

    @Test
    void findByIdShouldReturnEmptyForUnknownId() {
        assertThat(mpaStorage.findById(999)).isEmpty();
    }
}
