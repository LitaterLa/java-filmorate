package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpaa;
import ru.yandex.practicum.filmorate.repository.impl.JdbcMpaaRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MpaaService implements MpaaBaseService {
    private final JdbcMpaaRepository repository;

    @Override
    public List<Mpaa> getMpaa() {
        return repository.getAllRatings();
    }

    @Override
    public Mpaa getById(int id) {
        return repository.getById(id).orElseThrow(() -> new NotFoundException("Жанр не найден"));
    }
}
