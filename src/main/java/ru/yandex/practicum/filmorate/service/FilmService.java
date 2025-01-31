package ru.yandex.practicum.filmorate.service;

public interface FilmService {
    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    void delete(Long filmId);
}
