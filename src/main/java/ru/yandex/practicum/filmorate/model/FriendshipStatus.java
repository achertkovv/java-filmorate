package ru.yandex.practicum.filmorate.model;

public enum FriendshipStatus {
    /**
     * Один пользователь отправил заявку, второй ещё не согласился.
     */
    UNCONFIRMED,
    /**
     * Второй пользователь подтвердил заявку.
     */
    CONFIRMED;

    public boolean isConfirmed() {
        return this == CONFIRMED;
    }
}
