package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerValidationTest {

    private UserController controller;

    @BeforeEach
    void setup() {
        controller = new UserController();
    }

    private User validUser() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user_login");
        user.setName("Иван");
        user.setBirthday(LocalDate.of(1990, 5, 15));
        return user;
    }

    // ---------- Общий случай ----------

    @Test
    void shouldCreateValidUser() {
        User result = controller.createUser(validUser());
        assertNotNull(result.getId());
        assertEquals("user_login", result.getLogin());
    }

    @Test
    void shouldThrowWhenUserIsNull() {
        assertThrows(ValidationException.class, () -> controller.createUser(null));
    }

    // ---------- email ----------

    @Test
    void shouldRejectWhenEmailIsNull() {
        User user = validUser();
        user.setEmail(null);
        assertThrows(ConditionsNotMetException.class, () -> controller.createUser(user));
    }

    @Test
    void shouldRejectWhenEmailIsEmpty() {
        User user = validUser();
        user.setEmail("");
        assertThrows(ConditionsNotMetException.class, () -> controller.createUser(user));
    }

    @Test
    void shouldRejectWhenEmailIsBlank() {
        User user = validUser();
        user.setEmail("   ");
        assertThrows(ConditionsNotMetException.class, () -> controller.createUser(user));
    }

    @Test
    void shouldRejectWhenEmailHasNoAt() {
        User user = validUser();
        user.setEmail("userexample.com");
        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void shouldAcceptMinimalEmailWithAt() {
        User user = validUser();
        user.setEmail("a@b");
        assertDoesNotThrow(() -> controller.createUser(user));
    }

    // ---------- login ----------

    @Test
    void shouldRejectWhenLoginIsNull() {
        User user = validUser();
        user.setLogin(null);
        assertThrows(ConditionsNotMetException.class, () -> controller.createUser(user));
    }

    @Test
    void shouldRejectWhenLoginIsEmpty() {
        User user = validUser();
        user.setLogin("");
        assertThrows(ConditionsNotMetException.class, () -> controller.createUser(user));
    }

    @Test
    void shouldRejectWhenLoginIsBlank() {
        User user = validUser();
        user.setLogin("   ");
        assertThrows(ConditionsNotMetException.class, () -> controller.createUser(user));
    }

    @Test
    void shouldRejectWhenLoginContainsSpace() {
        User user = validUser();
        user.setLogin("user login");
        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void shouldAcceptLoginWithoutSpaces() {
        User user = validUser();
        user.setLogin("user_login123");
        assertDoesNotThrow(() -> controller.createUser(user));
    }

    // ---------- name ----------

    @Test
    void shouldSubstituteLoginWhenNameIsNull() {
        User user = validUser();
        user.setName(null);
        User result = controller.createUser(user);
        assertEquals("user_login", result.getName(),
                "Пустое имя → должно подставиться логин");
    }

    @Test
    void shouldSubstituteLoginWhenNameIsBlank() {
        User user = validUser();
        user.setName("   ");
        User result = controller.createUser(user);
        assertEquals("user_login", result.getName());
    }

    // ---------- birthday ----------

    @Test
    void shouldRejectWhenBirthdayIsNull() {
        User user = validUser();
        user.setBirthday(null);
        assertThrows(ConditionsNotMetException.class, () -> controller.createUser(user));
    }

    @Test
    void shouldAcceptWhenBirthdayIsToday() {
        User user = validUser();
        user.setBirthday(LocalDate.now());
        assertDoesNotThrow(() -> controller.createUser(user),
                "Сегодняшняя дата — граница допустимого");
    }

    @Test
    void shouldRejectWhenBirthdayIsTomorrow() {
        User user = validUser();
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    @Test
    void shouldRejectWhenBirthdayIsFarInFuture() {
        User user = validUser();
        user.setBirthday(LocalDate.of(2999, 1, 1));
        assertThrows(ValidationException.class, () -> controller.createUser(user));
    }

    // ---------- update ----------

    @Test
    void shouldRejectUpdateWhenIdIsNull() {
        assertThrows(ConditionsNotMetException.class,
                () -> controller.updateUser(validUser()));
    }

    @Test
    void shouldRejectUpdateWhenUserNotFound() {
        User user = validUser();
        user.setId(999L);
        assertThrows(NotFoundException.class, () -> controller.updateUser(user));
    }
}