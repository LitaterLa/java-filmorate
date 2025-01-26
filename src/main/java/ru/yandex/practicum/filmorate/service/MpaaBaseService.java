package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Mpaa;

import java.util.List;

public interface MpaaBaseService {
    List<Mpaa> getMpaa();

    Mpaa getById(int id);
}
