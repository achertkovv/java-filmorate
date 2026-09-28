package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;

    @Autowired
    public UserController(UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAllUsers() {
        List<User> users = userStorage.findAll();
        log.info("Запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users;
    }

    // Используйте аннотацию @RequestBody, чтобы создать объект из тела запроса на добавление или обновление сущности.
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Запрос на создание пользователя с логином: {}", user != null ? user.getLogin() : "null");
        validateUser(user);
        normalizeName(user);
        return userStorage.add(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("Запрос на обновление пользователя: id={}", user != null ? user.getLogin() : "null");
        validateUser(user);
        // Если при изменении данных пользователя не указан его идентификатор, то должно генерироваться
        // исключение ConditionsNotMetException с текстом: "Id должен быть указан".
        if (user.getId() == null) {
            log.warn("ID для пользователя '{}' не задан", user.getName());
            throw new ConditionsNotMetException("ID должен быть указан");
        }
        if (userStorage.findById(user.getId()).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден");
        }
        normalizeName(user);
        return userStorage.update(user);
    }

    // PUT /users/{id}/friends/{friendId} — добавление в друзья.
    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.addFriend(id, friendId);
    }

    // DELETE /users/{id}/friends/{friendId} — удаление из друзей.
    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        userService.removeFriend(id, friendId);
    }

    // GET /users/{id}/friends — возвращаем список пользователей, являющихся его друзьями.
    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        return userService.getFriends(id);
    }

    // GET /users/{id}/friends/common/{otherId} — список друзей, общих с другим пользователем.
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    private void validateUser(User user) {
        if (user == null) {
            log.warn("Попытка работать с пользователем null");
            throw new ValidationException("Пользователь не может быть null");
        }
        if (user.getBirthday() == null) {
            log.warn("Пустой день рождения");
            throw new ConditionsNotMetException("День рождения не может быть пустым");
        }
        // дата рождения не может быть в будущем.
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем '{}'", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        // логин не может быть пустым;
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Пустой логин");
            throw new ConditionsNotMetException("Логин должен быть указан");
        }
        // логин не может содержать пробелы;
        if (user.getLogin().contains(" ")) {
            log.warn("Логин пользователя '{}' содержит пробел", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
        // электронная почта не может быть пустой;
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Пустой email");
            throw new ConditionsNotMetException("Email должен быть указан");
        }
        // электронная почта должна содержать символ @;
        if (!user.getEmail().contains("@")) {
            log.warn("Некорректная email пользователя '{}': {}", user.getName(), user.getEmail());
            throw new ValidationException("Email должен содержать символ @");
        }
    }

    private void normalizeName(User user) {
        // имя для отображения может быть пустым — в таком случае будет использован логин;
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя '{}' пустое — используем логин", user.getLogin());
            user.setName(user.getLogin());
        }
    }
}
