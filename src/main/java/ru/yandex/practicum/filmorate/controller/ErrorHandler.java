package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ErrorHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(err -> {
            String msg = err.getDefaultMessage() != null
                    ? err.getDefaultMessage()
                    : "Некорректное значение";
            errors.put(err.getField(), msg);
            log.warn("Ошибка валидации поля '{}': {}", err.getField(), msg);
        });
        return errors;
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidation(ValidationException e) {
        log.warn("Ошибка валидации: {}", e.getMessage());
        return errorBody(e.getMessage(), "Ошибка валидации");
    }

    @ExceptionHandler(ConditionsNotMetException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    public Map<String, String> handleCondition(ConditionsNotMetException e) {
        log.warn("Ошибка условий: {}", e.getMessage());
        return errorBody(e.getMessage(), "Ошибка условия");
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public Map<String, String> handleNotFound(NotFoundException e) {
        log.warn("Не найдено: {}", e.getMessage());
        return errorBody(e.getMessage(), "Ресурс не найден");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleUnreadable(HttpMessageNotReadableException e) {
        log.warn("Некорректное тело запроса: {}", e.getMessage());
        return Map.of("error", "Некорректное тело запроса");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, String> handleOther(Exception e) {
        log.error("Непредвиденная ошибка", e);
        return errorBody(e.getMessage(), "Внутренняя ошибка сервера");
    }

    private Map<String, String> errorBody(String message, String alterText) {
        return Map.of("error", message != null ? message : alterText);
    }
}
