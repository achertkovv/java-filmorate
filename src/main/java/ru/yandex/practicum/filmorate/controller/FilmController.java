package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    // Надо исправить: Здесь то же самое: добавление, обновление и проверка «фильм не найден» идут в обход FilmService.
    // Их стоит перенести в сервис, чтобы путь «контроллер → сервис → хранилище» был единым.
    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return filmService.getAll();
    }

    // Используйте аннотацию @RequestBody, чтобы создать объект из тела запроса на добавление или обновление сущности.
    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        log.info("POST /films");
        return filmService.create(film);
    }

    // Используйте аннотацию @RequestBody, чтобы создать объект из тела запроса на добавление или обновление сущности.
    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        log.info("PUT /films id={}", film != null ? film.getId() : "null");
        return filmService.update(film);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable Long id) {
        return filmService.getById(id);
    }

    // PUT /films/{id}/like/{userId} — пользователь ставит лайк фильму.
    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    // DELETE /films/{id}/like/{userId} — пользователь удаляет лайк.
    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    // GET /films/popular?count={count} — возвращает список из первых count фильмов по количеству лайков.
    // Если значение параметра count не задано, верните первые 10
    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopular(count);
    }
}
