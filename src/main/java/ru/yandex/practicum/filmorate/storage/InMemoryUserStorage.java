package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {
    // Для хранения данных используйте HashMap.
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User add(User user) {
        // формируем дополнительные данные
        user.setId(getNextId());
        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @Override
    public User update(User user) {
        User oldUser = users.get(user.getId());
        oldUser.setName(user.getName());
        oldUser.setBirthday(user.getBirthday());
        oldUser.setEmail(user.getEmail());
        oldUser.setLogin(user.getLogin());
        log.info("Пользователь успешно обновлён: id={}, login={}", user.getId(), user.getLogin());
        return oldUser;
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriend(Long userId, Long friendId, FriendshipStatus status) {
        User user = users.get(userId);
        user.getFriends().put(friendId, status);
    }

    @Override
    public Optional<FriendshipStatus> getFriendshipStatus(Long userId, Long friendId) {
        User user = users.get(userId);
        Map<Long, FriendshipStatus> userFriends = user.getFriends();
        if (userFriends == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userFriends.get(friendId));
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        User user = users.get(userId);
        user.getFriends().remove(friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        User user = users.get(userId);
        return user.getFriends().keySet().stream()
                .map(id -> findById(id)
                        .orElseThrow(() -> new NotFoundException("Друг с id=" + id + " не найден")))
                .collect(Collectors.toList());
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        User user = users.get(userId);
        User other = users.get(otherId);

        Set<Long> commonIds = user.getFriends().keySet().stream()
                .filter(other.getFriends().keySet()::contains)
                .collect(Collectors.toSet());

        return commonIds.stream()
                .map(id -> findById(id)
                        .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден")))
                .collect(Collectors.toList());
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
