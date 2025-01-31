package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.repository.impl.JdbcFilmRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcGenreRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcMpaaRepository;
import ru.yandex.practicum.filmorate.repository.impl.JdbcUserRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaseFilmService implements FilmService {
    private final JdbcFilmRepository filmRepository;
    private final JdbcUserRepository userRepository;
    private final JdbcMpaaRepository mpaaRepository;
    private final JdbcGenreRepository genreRepository;
    private final EventService eventService;

    public Film save(Film film) {
        mpaaRepository.getById(film.getMpa().getId())
                .orElseThrow(() -> new NotFoundException("Rating was not found"));

        Set<Genre> genres = film.getGenres();
        if (genres != null && !genres.isEmpty()) {
            Set<Integer> genreIds = genres.stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            List<Genre> genresAlready = genreRepository.getByIds(genreIds);

            Map<Integer, Genre> genreMap = genresAlready.stream()
                    .collect(Collectors.toMap(Genre::getId, genre -> genre));

            genres.forEach(genre -> {
                if (!genreMap.containsKey(genre.getId())) {
                    throw new NotFoundException("Жанр " + genre.getId() + " не найден");
                }
            });
        }

        return filmRepository.save(film);
    }

    public Film update(Film newFilm) {
        filmRepository.get(newFilm.getId()).orElseThrow(() -> new NotFoundException("Фильм не найден"));
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
        eventService.createEvent(userId, filmId, UserEvent.EventType.LIKE, UserEvent.EventOperation.ADD);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        Film film = getFilmByIdOrThrow(filmId);
        User user = getUserByIdOrThrow(userId);
        filmRepository.removeLike(film, user);
        eventService.createEvent(userId, filmId, UserEvent.EventType.LIKE, UserEvent.EventOperation.REMOVE);
    }

    public Collection<Film> getFilmsByDirector(Long directorId, String sortType) {
        return filmRepository.findByDirector(directorId, sortType);
    }

    @Override
    public void delete(Long filmId) {
        getFilmByIdOrThrow(filmId);
        filmRepository.delete(filmId);
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
