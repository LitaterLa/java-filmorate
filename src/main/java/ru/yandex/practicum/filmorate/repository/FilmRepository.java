package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmRepository {
    Film save(Film film);

    void delete(Long id);

    Film update(Film film);

    Optional<Film> get(Long id);

    Collection<Film> getAll();
}