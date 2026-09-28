package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    // Для хранения данных используйте HashMap.
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users.values();
    }

    // Используйте аннотацию @RequestBody, чтобы создать объект из тела запроса на добавление или обновление сущности.
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        validateUserIsNull(user);
        log.info("Запрос на создание пользователя с логином: {}", user.getLogin());
        validateEmail(user);
        validateLogin(user);
        normalizeName(user);
        validateBirthday(user);
        // формируем дополнительные данные
        user.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        validateUserIsNull(newUser);
        log.info("Запрос на обновление пользователя: id={}", newUser.getId());
        // Если при изменении данных пользователя не указан его идентификатор, то должно генерироваться
        // исключение ConditionsNotMetException с текстом: "Id должен быть указан".
        if (newUser.getId() == null) {
            log.warn("ID для пользователя '{}' не задан", newUser.getName());
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            validateEmail(newUser);
            validateLogin(newUser);
            validateBirthday(newUser);
            normalizeName(newUser);
            oldUser.setName(newUser.getName());
            oldUser.setBirthday(newUser.getBirthday());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setLogin(newUser.getLogin());
            log.info("Пользователь успешно обновлён: id={}, login={}", newUser.getId(), newUser.getLogin());
            return oldUser;
        }
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    private void validateUserIsNull(User user) {
        if (user == null) {
            log.warn("Попытка работать с пользователем null");
            throw new ValidationException("Пользователь не может быть null");
        }
    }

    private void validateBirthday(User user) {
        // дата рождения не может быть в будущем.
        if (user.getBirthday() == null) {
            log.warn("Пустой день рождения");
            throw new ConditionsNotMetException("День рождения не может быть пустым");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Дата рождения не может быть в будущем '{}'", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void validateLogin(User user) {
        // логин не может быть пустым и содержать пробелы;
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Пустой логин");
            throw new ConditionsNotMetException("Логин должен быть указан");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Логин пользователя '{}' содержит пробел", user.getLogin());
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }

    private void validateEmail(User user) {
        // электронная почта не может быть пустой и должна содержать символ @;
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Пустой email");
            throw new ConditionsNotMetException("Email должен быть указан");
        }
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

    // Метод getNextId для генерации идентификатора пользователя при создании аккаунта
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
