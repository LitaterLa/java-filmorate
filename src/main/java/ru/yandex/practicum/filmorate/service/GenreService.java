package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService implements BaseGenreService {
    private final GenreRepository genreRepository;

    @Override
    public Genre getGenreById(long id) {
        return genreRepository.getById(id).orElseThrow(() -> new NotFoundException("Жанр не найден"));
    }

    @Override
    public List<Genre> getAllGenres() {
        return genreRepository.getAll();
    }
}
