package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper = new UserRowMapper();

    @Override
    public User add(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setObject(4, user.getBirthday());
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKeyAs(Long.class));
        log.debug("Пользователь добавлен в БД: id={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday(), user.getId());
        if (rowsUpdated == 0) {
            throw new EmptyResultDataAccessException(1);
        }
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, userRowMapper, id));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM users", userRowMapper);
    }

    @Override
    public void addFriend(Long userId, Long friendId, FriendshipStatus status) {
        // Для PostgreSQL MERGE заменяется на
        // INSERT ... ON CONFLICT (following_user_id, followed_user_id)
        // DO UPDATE SET confirmed_friendship = EXCLUDED.confirmed_friendship
        String sql = "MERGE INTO user_friends (following_user_id, followed_user_id, confirmed_friendship) " +
                "KEY (following_user_id, followed_user_id) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, userId, friendId, status.isConfirmed());
    }

    @Override
    public Optional<FriendshipStatus> getFriendshipStatus(Long userId, Long friendId) {
        String sql = "SELECT confirmed_friendship FROM user_friends " +
                "WHERE following_user_id = ? AND followed_user_id = ?";
        try {
            Boolean status = jdbcTemplate.queryForObject(sql, Boolean.class, userId, friendId);
            return Optional.of(Boolean.TRUE.equals(status) ? FriendshipStatus.CONFIRMED : FriendshipStatus.UNCONFIRMED);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM user_friends WHERE following_user_id = ? AND followed_user_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN user_friends f ON u.id = f.followed_user_id " +
                "WHERE f.following_user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN user_friends f1 ON u.id = f1.followed_user_id " +
                "JOIN user_friends f2 ON u.id = f2.followed_user_id " +
                "WHERE f1.following_user_id = ? AND f2.following_user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, userId, otherId);
    }
}
