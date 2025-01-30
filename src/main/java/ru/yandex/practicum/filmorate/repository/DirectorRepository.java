package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;

public interface DirectorRepository {

    Collection<Director> findAll();

    Director findById(Long id);

    Director save(Director director);

    Director update(Director director);

    void delete(Long id);
}
