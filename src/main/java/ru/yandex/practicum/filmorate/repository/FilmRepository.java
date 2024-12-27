package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface FilmRepository {
    Film save(Film film);

    void delete(Long id);

    Film update(Film film);

    Optional<Film> get(Long id);

    Collection<Film> getAll();

    void addLike(Film film, User user);

    void removeLike(Film film, User user);

    Set<Long> getUsersLikes(Long filmId);
}
