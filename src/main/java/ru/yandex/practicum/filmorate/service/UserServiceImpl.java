package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.impl.JdbcFilmRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcUserRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final JdbcUserRepository userRepository;
    private final JdbcFilmRepository filmRepository;

    public User save(User user) {
        userRepository.save(user);
        return user;
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        final User user = getByIdOrThrow(userId);
        final User friend = getByIdOrThrow(friendId);
        if (userRepository.getFriends(userId).contains(user)) {
            throw new ValidationException("Уже друг");
        }
        userRepository.addFriend(user.getId(), friend.getId());
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        final User user = getByIdOrThrow(userId);
        final User friend = getUserById(friendId);
        userRepository.deleteFriend(user.getId(), friend.getId());
        userRepository.getFriends(userId);
    }

    @Override
    public Set<User> getFriends(Long userId) {
        User user = userRepository.get(userId).orElseThrow(() -> new NotFoundException("User not found"));
        return new HashSet<>(userRepository.getFriends(user.getId()));
    }

    @Override
    public User getUserById(Long id) {
        return getByIdOrThrow(id);
    }

    @Override
    public void delete(Long id) {
        userRepository.delete(id);
    }

    public User update(User newUser) {
        if (userRepository.get(newUser.getId()).isEmpty()) throw new NotFoundException("Пользователь не бы найден");
        return userRepository.update(newUser);
    }

    public Collection<User> getAllUsers() {
        return userRepository.getAll();
    }

    public Set<User> getMutualFriends(Long id1, Long id2) {
        return userRepository.getMutualFriends(id1, id2);
    }

    private User getByIdOrThrow(Long id) {
        return userRepository.get(id).orElseThrow(() -> new NotFoundException(" не найден Пользователь ID=" + id));
    }

    public Collection<Film> getFilmRecommendations(Long userId) {
        return filmRepository.findFilmByUserLikes(userId);
    }
}