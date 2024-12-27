package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.InMemoryUserRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final InMemoryUserRepository userRepository;

    public User save(User user) {
        userRepository.save(user);
        return user;
    }

    @Override
    public Set<Long> addFriend(Long userId, Long friendId) {
        final User user = getUserByIdInternal(userId);
        final User friend = getUserByIdInternal(friendId);
        if (userRepository.getFriends(userId).contains(friendId)) {
            throw new ValidationException("Уже друг");
        }
        userRepository.addFriend(user, friend);
        return userRepository.getFriends(userId);
    }

    @Override
    public Set<Long> deleteFriend(Long userId, Long friendId) {
        final User user = getUserByIdInternal(userId);
        final User friend = getUserById(friendId);
        if (!(userRepository.getFriends(userId).contains(friendId))) {
            throw new NotFoundException("Ошибка: друг не найден");
        }
        userRepository.deleteFriend(user, friend);
        return userRepository.getFriends(userId);
    }

    @Override
    public Set<Long> getFriends(Long userId) {
        return new HashSet<>(userRepository.getFriends(userId));
    }

    @Override
    public User getUserById(Long id) {
        User user = getUserByIdInternal(id);
        return user;
    }

    public User update(User newUser) {
        return userRepository.update(newUser);
    }

    public Collection<User> getAllUsers() {
        return userRepository.getAll();
    }

    public Set<User> getMutualFriends(Long id1, Long id2) {
        return userRepository.getMutualFriends(id1, id2);
    }

    private User getUserByIdInternal(Long id) {
        return userRepository.get(id).orElseThrow(() -> new NotFoundException(" не найден Пользователь ID=" + id));
    }
}
