package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserRepository;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Имя пользователя не может быть пустым");
        }
        log.info("валидация пользователя при создании: {}", user);
        user.setId(repository.generateId());
        log.info("добавление пользователя ID {}", user.getId());
        repository.save(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return Optional.ofNullable(repository.get(newUser.getId())).map(oldUser -> {
            log.info("валидация пользователья ID {} при обновлении", newUser.getId());
            oldUser.setLogin(newUser.getLogin());
            oldUser.setName(newUser.getName());
            oldUser.setEmail(newUser.getEmail());
            oldUser.setBirthday(newUser.getBirthday());
            log.info("обновление пользователя ID {}", newUser.getId());
            return oldUser;
        }).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("получение всех пользователей");
        return repository.getAll();
    }

}
