package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

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
}
