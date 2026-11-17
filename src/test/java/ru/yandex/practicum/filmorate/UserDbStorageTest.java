package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.UserDbStorage;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userStorage;

    private User newUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void addShouldPersistUserAndAssignId() {
        User saved = userStorage.add(newUser("a@b.c", "alice"));

        assertThat(saved.getId()).isNotNull();

        assertThat(userStorage.findById(saved.getId()))
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(saved.getId());
                    assertThat(u.getEmail()).isEqualTo("a@b.c");
                    assertThat(u.getLogin()).isEqualTo("alice");
                    assertThat(u.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    void testFindUserById() {
        User alice = userStorage.add(newUser("a@b.c", "alice"));

        long id = alice.getId();

        Optional<User> userOptional = userStorage.findById(id);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", id));
    }

    @Test
    void findByIdShouldReturnEmptyForUnknown() {
        assertThat(userStorage.findById(999L)).isEmpty();
    }

    @Test
    void findAllShouldReturnAllUsers() {
        userStorage.add(newUser("a@b.c", "alice"));
        userStorage.add(newUser("b@b.c", "bob"));
        userStorage.add(newUser("c@b.c", "carol"));

        assertThat(userStorage.findAll())
                .hasSize(3)
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("alice", "bob", "carol");
    }

    @Test
    void updateShouldModifyUser() {
        User saved = userStorage.add(newUser("a@b.c", "alice"));

        saved.setEmail("new@b.c");
        saved.setName("Alice");
        userStorage.update(saved);

        assertThat(userStorage.findById(saved.getId()))
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getEmail()).isEqualTo("new@b.c");
                    assertThat(u.getName()).isEqualTo("Alice");
                });
    }

    @Test
    void addFriendShouldCreateOneWayFriendship() {
        User alice = userStorage.add(newUser("a@b.c", "alice"));
        User bob = userStorage.add(newUser("b@b.c", "bob"));

        userStorage.addFriend(alice.getId(), bob.getId(), FriendshipStatus.UNCONFIRMED);

        // у Алисы друг есть
        assertThat(userStorage.getFriends(alice.getId()))
                .extracting(User::getId)
                .containsExactly(bob.getId());

        // у Боба Алисы нет — дружба односторонняя
        assertThat(userStorage.getFriends(bob.getId())).isEmpty();
    }

    @Test
    void getFriendshipStatusShouldReturnStatus() {
        User alice = userStorage.add(newUser("a@b.c", "alice"));
        User bob = userStorage.add(newUser("b@b.c", "bob"));

        userStorage.addFriend(alice.getId(), bob.getId(), FriendshipStatus.CONFIRMED);

        assertThat(userStorage.getFriendshipStatus(alice.getId(), bob.getId()))
                .contains(FriendshipStatus.CONFIRMED);

        // обратного статуса нет
        assertThat(userStorage.getFriendshipStatus(bob.getId(), alice.getId()))
                .isEmpty();
    }

    @Test
    void getFriendshipStatusEmptyForUnknownRelationship() {
        User alice = userStorage.add(newUser("a@b.c", "alice"));
        User bob = userStorage.add(newUser("b@b.c", "bob"));

        assertThat(userStorage.getFriendshipStatus(alice.getId(), bob.getId())).isEmpty();
    }

    @Test
    void removeFriendshipShouldDeleteOneWayLink() {
        User alice = userStorage.add(newUser("a@b.c", "alice"));
        User bob = userStorage.add(newUser("b@b.c", "bob"));

        userStorage.addFriend(alice.getId(), bob.getId(), FriendshipStatus.CONFIRMED);
        userStorage.removeFriend(alice.getId(), bob.getId());

        assertThat(userStorage.getFriends(alice.getId())).isEmpty();
        assertThat(userStorage.getFriendshipStatus(alice.getId(), bob.getId())).isEmpty();
    }

    @Test
    void getCommonFriendsShouldReturnIntersection() {
        User alice = userStorage.add(newUser("a@b.c", "alice"));
        User bob = userStorage.add(newUser("b@b.c", "bob"));
        User carol = userStorage.add(newUser("c@b.c", "carol"));
        User dave = userStorage.add(newUser("d@b.c", "dave"));

        // Алиса: carol, dave; Боб: carol
        userStorage.addFriend(alice.getId(), carol.getId(), FriendshipStatus.CONFIRMED);
        userStorage.addFriend(alice.getId(), dave.getId(), FriendshipStatus.CONFIRMED);
        userStorage.addFriend(bob.getId(), carol.getId(), FriendshipStatus.CONFIRMED);

        List<User> common = userStorage.getCommonFriends(alice.getId(), bob.getId());

        assertThat(common)
                .extracting(User::getId)
                .containsExactly(carol.getId());
    }

    @Test
    void getCommonFriendsEmptyWhenNoOverlap() {
        User alice = userStorage.add(newUser("a@b.c", "alice"));
        User bob = userStorage.add(newUser("b@b.c", "bob"));
        User carol = userStorage.add(newUser("c@b.c", "carol"));
        User dave = userStorage.add(newUser("d@b.c", "dave"));

        userStorage.addFriend(alice.getId(), carol.getId(), FriendshipStatus.CONFIRMED);
        userStorage.addFriend(bob.getId(), dave.getId(), FriendshipStatus.CONFIRMED);

        assertThat(userStorage.getCommonFriends(alice.getId(), bob.getId())).isEmpty();
    }
}
