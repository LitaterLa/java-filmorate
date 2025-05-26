package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface GenreRepository {

    Optional<Genre> getById(long id);

    List<Genre> getAll();

    Map<Long, Set<Genre>> getGenresByFilmIds(Set<Long> filmIds);
}
