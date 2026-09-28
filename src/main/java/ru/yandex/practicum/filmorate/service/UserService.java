package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        validateUser(user);
        normalizeName(user);
        log.info("Запрос на создание пользователя с логином: {}", user.getLogin());
        return userStorage.add(user);
    }

    public User update(User user) {
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
        log.info("Запрос на обновление пользователя: id={}", user.getId());
        return userStorage.update(user);
    }

    public List<User> getAll() {
        List<User> users = userStorage.findAll();
        log.info("Запрос на получение всех пользователей. Текущее количество: {}", users.size());
        return users;
    }

    // Добавление в друзья
    public void addFriend(Long userId, Long friendId) {
        User user = getExistingUser(userId);
        User friend = getExistingUser(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(userId);

        log.info("Пользователи {} и {} теперь друзья", userId, friendId);
    }

    // Удаление из друзей
    public void removeFriend(Long userId, Long friendId) {
        User user = getExistingUser(userId);
        User friend = getExistingUser(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);

        log.info("Пользователи {} и {} больше не друзья", userId, friendId);
    }

    // Список друзей пользователя.
    public List<User> getFriends(Long userId) {
        User user = getExistingUser(userId);
        return user.getFriends().stream()
                .map(id -> userStorage.findById(id)
                        .orElseThrow(() -> new NotFoundException("Друг с id=" + id + " не найден")))
                .collect(Collectors.toList());
    }

    // Общие друзья двух пользователей
    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = getExistingUser(userId);
        User other = getExistingUser(otherId);

        Set<Long> commonIds = user.getFriends().stream()
                .filter(other.getFriends()::contains)
                .collect(Collectors.toSet());

        return commonIds.stream()
                .map(id -> userStorage.findById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден")))
                .collect(Collectors.toList());
    }

    private User getExistingUser(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    // Логику поиска удобно держать в сервисе, там уже есть похожий приватный метод.
    public User getById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
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
