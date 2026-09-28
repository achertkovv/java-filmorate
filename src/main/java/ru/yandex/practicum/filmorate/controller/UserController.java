package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    // Надо исправить: Контроллер зависит сразу от UserStorage и UserService: создание, обновление, проверка
    // существования и подстановка логина вместо имени идут прямо через хранилище. По заданию контроллер
    // только принимает запрос и передаёт его в сервис, а работа с хранилищем — задача сервиса.
    // Эти операции стоит перенести в UserService и оставить в контроллере одну зависимость.
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAllUsers() {
        return userService.getAll();
    }

    // Используйте аннотацию @RequestBody, чтобы создать объект из тела запроса на добавление или обновление сущности.
    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("POST /users");
        return userService.create(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        log.info("PUT /users id={}", user != null ? user.getId() : "null");
        return userService.update(user);
    }

    // Надо исправить: По заданию нужен эндпоинт получения пользователя по id:
    // GET /users/{id} через @PathVariable,
    // с ответом 404 для несуществующего id. В контроллере его нет.
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return userService.getById(id);
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
}
