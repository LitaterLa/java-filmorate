package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmRepository {
    Film save(Film film);

    void delete(Long id);

    Film update(Film film);

    Optional<Film> get(Long id);

    Collection<Film> getAll();

    void addLike(Film film, User user);

    void removeLike(Film film, User user);

    List<Film> findBestLiked(Integer count);

    List<Film> findCommonFilms(Long userId, Long friendId);

    List<Film> findPopularFilms(int count, Integer genreId, Integer year);

    Collection<Film> findByDirector(Long directorId, String sortType);
}
