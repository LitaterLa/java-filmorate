package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Set;

public interface UserService {
    Set<Long> addFriend(Long userId, Long friendId);

    Set<Long> deleteFriend(Long userId, Long friendId);

    Set<Long> getFriends(Long userId);

    User getUserById(Long id);
}
