package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DirectorRepository {

    List<Director> findAll();

    Director findById(Long id);

    Director save(Director director);

    Director update(Director director);

    void delete(Long id);

    Map<Long, Set<Director>> getDirectorsByFilmIds(Set<Long> filmIds);
}
