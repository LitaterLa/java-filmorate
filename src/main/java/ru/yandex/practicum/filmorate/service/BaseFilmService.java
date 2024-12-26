package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.InMemoryFilmRepository;
import ru.yandex.practicum.filmorate.repository.InMemoryUserRepository;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaseFilmService implements FilmService {
    final InMemoryFilmRepository filmStorage;
    final InMemoryUserRepository userStorage;

    public Film save(Film film) {
        return filmStorage.save(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Collection<Film> getALl() {
        return filmStorage.getAll();
    }

    @Override
    public Set<Long> addLike(Long filmId, Long userId) {
        Film film = filmStorage.get(filmId).orElseThrow(() ->
                new NotFoundException("not found film ID=" + filmId));
        User user = userStorage.get(userId).orElseThrow(() -> new NotFoundException("Not found user ID=" + userId));
        filmStorage.addLike(film, user);
        return filmStorage.getUsersLikes(filmId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        Film film = filmStorage.get(filmId).orElseThrow(() ->
                new NotFoundException("not found film ID=" + filmId));
        User user = userStorage.get(userId).orElseThrow(() -> new NotFoundException("Not found user ID=" + userId));
        filmStorage.removeLike(film, user);
    }

    public List<Film> findBestLiked(Integer count) {
        return filmStorage.getAll().stream()
                .sorted(Comparator.comparingInt((Film film) -> filmStorage.getUsersLikes(film.getId()).size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}
