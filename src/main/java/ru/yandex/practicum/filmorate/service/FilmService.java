package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    void delete(Long filmId);

    List<Film> findMostPopularFilms(Integer count, Integer genreId, Integer year);

    void loadGenresForFilms(List<Film> films);

    void loadDirectorsForFilms(List<Film> films);
}
