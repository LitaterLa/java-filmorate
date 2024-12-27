package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Optional;
import java.util.Set;

public interface UserRepository {
    void save(User user);

    void delete(Long id);

    User update(User user);

    Optional<User> get(Long id);

    Set<Long> addFriend(User user, User friend);

    void deleteFriend(User user, User friend);

    Set<Long> getFriends(Long userId);

    Set<User> getMutualFriends(Long id1, Long id2);
}
