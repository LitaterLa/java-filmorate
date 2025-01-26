package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface BaseGenreService {
    Genre getGenreById(long id);

    List<Genre> getAllGenres();
}
