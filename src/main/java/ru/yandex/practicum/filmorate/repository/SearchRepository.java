package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface SearchRepository {
    List<Film> searchFilm(String query, String searchBy);
}
