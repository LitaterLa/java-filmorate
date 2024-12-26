package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

public interface UserService {
    void addFriend(Long userId, Long friendId);

    Set<User> deleteFriend(Long userId, Long friendId);

    Set<User> getFriends(Long userId);

    User getUserById(Long id);
}
