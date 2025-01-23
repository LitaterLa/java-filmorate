package ru.yandex.practicum.filmorate.integralTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.impl.JdbcUserRepository;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({JdbcUserRepository.class, UserRowMapper.class})
public class UserDBRepositoryTest {
    private final JdbcUserRepository userRepository;
    private User user = new User("sampleLogin1", "Sample Name1", "sample1@email.com", LocalDate.of(1900, 1, 1));
    private User user2 = new User("sampleLogin2", "Sample Name2", "sample2@email.com", LocalDate.of(1900, 1, 1));
    private User user3 = new User("sampleLogin3", "Sample Name3", "sample3@email.com", LocalDate.of(1900, 1, 1));
    private User user4 = new User("sampleLogin4", "Sample Name4", "sample4@email.com", LocalDate.of(1900, 1, 1));


    @Test
    public void testSaveUser() {
        User savedUser = userRepository.save(user);
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedUser);
    }


    @Test
    public void testFindUserById() {
        User saved = userRepository.save(user);
        Long savedId = saved.getId();
        Optional<User> userOptional = userRepository.get(savedId);
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(userIn ->
                        assertThat(userIn).hasFieldOrPropertyWithValue("id", savedId)
                );
    }

    @Test
    public void testUpdate() {
        User savedUser = userRepository.save(user);
        savedUser.setName("NewName");
        savedUser.setEmail("newSample@email.com");
        savedUser.setLogin("NewLogin");
        savedUser.setBirthday(LocalDate.of(2000, 1, 1));
        userRepository.update(savedUser);
        assertThat(savedUser).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedUser);
    }

    @Test
    public void testGetAll() {
        userRepository.save(user);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);
        List<User> users = userRepository.getAll();
        assertThat(users).hasSize(4);

        assertThat(users).extracting(User::getLogin).containsExactlyInAnyOrder(
                "sampleLogin1", "sampleLogin2", "sampleLogin3", "sampleLogin4"
        );

        assertThat(users).extracting(User::getName).containsExactlyInAnyOrder(
                "Sample Name1", "Sample Name2", "Sample Name3", "Sample Name4"
        );

        assertThat(users).extracting(User::getEmail).containsExactlyInAnyOrder(
                "sample1@email.com", "sample2@email.com", "sample3@email.com", "sample4@email.com"
        );
    }

    @Test
    public void testAddFriend() {
        User savedUser1 = userRepository.save(user);
        User savedUser2 = userRepository.save(user2);
        User savedUser3 = userRepository.save(user3);

        userRepository.addFriend(savedUser1.getId(), savedUser2.getId());
        userRepository.addFriend(savedUser1.getId(), savedUser3.getId());

        Set<User> friends = userRepository.getFriends(savedUser1.getId());
        assertThat(friends).hasSize(2);

        assertThat(friends).extracting(User::getId).containsExactlyInAnyOrder(
                savedUser2.getId(), savedUser3.getId()
        );
    }

    @Test
    public void testDeleteFriend() {
        User savedUser1 = userRepository.save(user);
        User savedUser2 = userRepository.save(user2);
        User savedUser3 = userRepository.save(user3);

        Long id = savedUser1.getId();
        userRepository.addFriend(id, savedUser2.getId());
        userRepository.addFriend(id, savedUser3.getId());

        userRepository.deleteFriend(id, savedUser2.getId());
        Set<User> friends = userRepository.getFriends(id);

        assertThat(friends).hasSize(1);

        assertThat(friends).extracting(User::getId).contains(savedUser3.getId());
        assertThat(friends).doesNotContain(savedUser2);
    }


    @Test
    public void testGetMutualFriends() {
        User savedUser1 = userRepository.save(user);
        User savedUser2 = userRepository.save(user2);
        User savedUser3 = userRepository.save(user3);
        userRepository.addFriend(savedUser1.getId(), savedUser2.getId());
        userRepository.addFriend(savedUser1.getId(), savedUser3.getId());
        userRepository.addFriend(savedUser2.getId(), savedUser3.getId());
        Set<User> mutual = userRepository.getMutualFriends(savedUser1.getId(), savedUser2.getId());

        assertThat(mutual).isNotNull().hasSize(1).contains(savedUser3);

    }


}
