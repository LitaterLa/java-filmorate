package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.InMemoryFilmRepository;
import ru.yandex.practicum.filmorate.repository.InMemoryUserRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.Collection;
import java.util.List;

@Service
public class BaseFilmService implements FilmService {
    final FilmRepository filmStorage = new InMemoryFilmRepository();
    final UserRepository userStorage = new InMemoryUserRepository();

    public Film save(Film film) {
        return filmStorage.save(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        Film film = getFilmByIdOrThrow(filmId);
        User user = getUserByIdOrThrow(userId);
        filmStorage.addLike(film, user);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        Film film = getFilmByIdOrThrow(filmId);
        User user = getUserByIdOrThrow(userId);
        filmStorage.removeLike(film, user);
    }

    public List<Film> findBestLiked(Integer count) {
        return filmStorage.findBestLiked(count);
    }

    public Film getFilmByIdOrThrow(Long filmId) {
        return filmStorage.get(filmId).orElseThrow(() ->
                new NotFoundException("not found film ID=" + filmId));
    }

    private User getUserByIdOrThrow(Long userId) {
        return userStorage.get(userId).orElseThrow(() -> new NotFoundException("Not found user ID=" + userId));
    }

}
