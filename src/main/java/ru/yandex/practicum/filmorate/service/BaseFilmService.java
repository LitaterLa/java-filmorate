package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.impl.JdbcFilmRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcGenreRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcMpaaRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcUserRepository;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BaseFilmService implements FilmService {
    private final JdbcFilmRepository filmRepository;
    private final JdbcUserRepository userRepository;
    private final JdbcMpaaRepository mpaaRepository;
    private final JdbcGenreRepository genreRepository;

    public Film save(Film film) {
        mpaaRepository.getById(film.getMpa().getId())
                .orElseThrow(() -> new BadRequestException("Rating was not found"));
        LinkedHashSet<Genre> genres = film.getGenres();
        genres.forEach(genre -> genreRepository.getById(genre.getId())
                .orElseThrow(() -> new BadRequestException("Genre was not found")));
        return filmRepository.save(film);
    }

    public Film update(Film newFilm) {
        filmRepository.get(newFilm.getId()).orElseThrow(() -> new NotFoundException("Film not found"));
        return filmRepository.update(newFilm);
    }

    public Collection<Film> getAll() {
        return filmRepository.getAll();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = getFilmByIdOrThrow(filmId);
        User user = getUserByIdOrThrow(userId);
        filmRepository.addLike(film, user);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        Film film = getFilmByIdOrThrow(filmId);
        User user = getUserByIdOrThrow(userId);
        filmRepository.removeLike(film, user);
    }

    public List<Film> findBestLiked(Integer count) {
        return filmRepository.findBestLiked(count);
    }

    public Film getFilmByIdOrThrow(Long filmId) {
        return filmRepository.get(filmId).orElseThrow(() -> new NotFoundException("not found film ID=" + filmId));
    }

    private User getUserByIdOrThrow(Long userId) {
        return userRepository.get(userId).orElseThrow(() -> new NotFoundException("Not found user ID=" + userId));
    }

}
