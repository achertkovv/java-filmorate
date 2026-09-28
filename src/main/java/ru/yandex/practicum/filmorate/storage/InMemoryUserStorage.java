package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    // Для хранения данных используйте HashMap.
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public User add(User user) {
        // формируем дополнительные данные
        user.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
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
        // Можно лучше: update возвращает объект из запроса, а не сохранённый,
        // поэтому в ответе на PUT /users список друзей всегда пустой. В InMemoryFilmStorage это сделано правильно.
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
