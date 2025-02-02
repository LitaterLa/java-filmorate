package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.repository.DirectorRepository;
import ru.yandex.practicum.filmorate.repository.impl.*;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaseFilmService implements FilmService {
    private final JdbcFilmRepository filmRepository;
    private final JdbcUserRepository userRepository;
    private final JdbcMpaaRepository mpaaRepository;
    private final JdbcGenreRepository genreRepository;
    private final EventService eventService;
    private final SearchRepositoryImpl searchRepository;
    private final DirectorRepository directorRepository;

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

    public List<Film> getCommonFilms(Long userId, Long friendId) {
        userRepository.get(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден: userId=" + userId));
        userRepository.get(friendId).orElseThrow(() -> new NotFoundException("Пользователь не найден: friendId=" + friendId));

        return filmRepository.findCommonFilms(userId, friendId);
    }

    public List<Film> searchFilm(String query, String searchBy) {
        return searchRepository.searchFilm(query, searchBy);
    }

    private User getUserByIdOrThrow(Long userId) {
        return userRepository.get(userId).orElseThrow(() -> new NotFoundException("Not found user ID=" + userId));
    }

    @Override
    public void loadGenresForFilms(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        Map<Long, Set<Genre>> genresByFilmId = genreRepository.getGenresByFilmIds(
                films.stream()
                        .map(Film::getId)
                        .collect(Collectors.toSet())
        );

        for (Film film : films) {
            film.setGenres(new LinkedHashSet<>(genresByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>())));
        }
    }

    @Override
    public List<Film> findMostPopularFilms(Integer count, Integer genreId, Integer year) {
        log.info("Запрос популярных фильмов с фильтрацией: count={}, genreId={}, year={}", count, genreId, year);

        List<Film> films = filmRepository.findMostPopularFilms(count, genreId, year);

        if (!films.isEmpty()) {
            loadGenresForFilms(films);
            loadDirectorsForFilms(films);
        }

        log.info("Фильмы после загрузки жанров и режиссёров: {}", films);

        return films;
    }

    @Override
    public void loadDirectorsForFilms(List<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        Map<Long, Set<Director>> directorsByFilmId = directorRepository.getDirectorsByFilmIds(
                films.stream().map(Film::getId).collect(Collectors.toSet())
        );

        for (Film film : films) {
            film.setDirectors(new LinkedHashSet<>(directorsByFilmId.getOrDefault(film.getId(), new LinkedHashSet<>())));
        }
    }
}

