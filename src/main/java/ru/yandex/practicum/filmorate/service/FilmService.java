package ru.yandex.practicum.filmorate.service;

import java.util.Set;

public interface FilmService {
    Set<Long> addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);
}
