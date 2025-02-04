package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.Delete;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserServiceImpl;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    private final UserServiceImpl userService;
    private final EventService eventService;

    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public User get(@PathVariable Long userId) {
        log.info("Get user by id={}", userId);
        return userService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User save(@Validated(Create.class) @RequestBody User user) {
        log.info("добавление пользователя ID {}", user.getId());
        userService.save(user);
        return user;
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User update(@Validated(Update.class) @RequestBody User newUser) {
        log.info("обновление пользователя ID {}", newUser.getId());
        return userService.update(newUser);
    }

    @DeleteMapping("/{userId}")
    public void delete(@Validated(Delete.class) @PathVariable Long userId) {
        log.info("Удаление пользователя ID {}", userId);
        userService.delete(userId);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> getAll() {
        log.info("получение всех пользователей");
        return userService.getAllUsers();
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public Set<User> addFriend(@PathVariable Long id,
                               @PathVariable Long friendId) {
        log.info("Пользователь с id {} добавляет в друзья пользователя с id {}", id, friendId);
        userService.addFriend(id, friendId);
        return userService.getFriends(id);
    }


    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public Set<User> deleteFriend(@PathVariable Long id,
                                  @PathVariable Long friendId) {
        log.info("Пользователь с id {} удаляет из друзей пользователя с id {}", id, friendId);
        userService.deleteFriend(id, friendId);
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public Set<User> getAllFriends(@PathVariable Long id) {
        log.info("получение всех друзей пользователя");
        return userService.getFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    @ResponseStatus(HttpStatus.OK)
    public Collection<User> getMutualFriends(@PathVariable Long id,
                                             @PathVariable Long otherId) {
        log.info("получение общих друзей пользователя");
        return userService.getMutualFriends(id, otherId);
    }

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getFilmRecommendations(@PathVariable Long id) {
        return userService.getFilmRecommendations(id);
    }

    @GetMapping("/{id}/feed")
    @ResponseStatus(HttpStatus.OK)
    public Collection<UserEvent> getAllUserFeeds(@PathVariable @Positive Long id) {
        log.info("запрашиваются события пользователя с id {}", id);
        return eventService.getEventByUser(id);
    }
}