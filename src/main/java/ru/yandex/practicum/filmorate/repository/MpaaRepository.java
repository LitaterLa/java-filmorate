package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Mpaa;

import java.util.List;
import java.util.Optional;

public interface MpaaRepository {
    List<Mpaa> getAllRatings();

    Optional<Mpaa> getById(int id);
}
