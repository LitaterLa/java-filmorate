package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository {
    User save(User user);

    void delete(Long id);

    User update(User user);

    Optional<User> get(Long id);

    List<User> getAll();

    void addFriend(Long user, Long friend);

    void deleteFriend(Long user, Long friend);

    Set<User> getFriends(Long userId);

    Set<User> getMutualFriends(Long id1, Long id2);
}
