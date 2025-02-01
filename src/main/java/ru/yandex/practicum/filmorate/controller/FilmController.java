package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.BaseFilmService;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.util.Collection;
import java.util.List;


@RestController
@RequestMapping("/films")
@Validated
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final BaseFilmService filmService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film save(@Validated(Create.class) @RequestBody Film film) {
        Film savedFilm = filmService.save(film);
        log.info("сохранение фильма {} ID {}", savedFilm.getName(), savedFilm.getId());
        return savedFilm;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Collection<Film> getAll() {
        return filmService.getAll();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Film getFilm(@PathVariable @Positive Long id) {
        return filmService.getFilmByIdOrThrow(id);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film update(@Validated(Update.class) @RequestBody Film newFilm) {
        Film updatedFilm = filmService.update(newFilm);
        log.info("обновление фильма ID {}", updatedFilm.getId());
        return updatedFilm;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable @Positive Long id) {
        log.info("Удаление фильма ID {}", id);
        filmService.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable @Positive Long id,
                        @PathVariable @Positive Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable @Positive Long id,
                           @PathVariable @Positive Long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> findBestLiked(@RequestParam(value = "count", defaultValue = "10") @Positive Integer count) {
        return filmService.findBestLiked(count);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getFilmsByDirector(@PathVariable Long directorId,
                                               @RequestParam String sortBy) {
        return filmService.getFilmsByDirector(directorId, sortBy);
    }

    @GetMapping("/common")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getCommonFilms(@RequestParam @Positive Long userId,
                                     @RequestParam @Positive Long friendId) {
        log.info("Запрос общих фильмов для пользователей userId={} и friendId={}", userId, friendId);
        return filmService.getCommonFilms(userId, friendId);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> searchFilms(@RequestParam String query, @RequestParam (value = "by") String searchBy) {
        return filmService.searchFilm(query, searchBy);
    }

    @GetMapping("/films/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getMostPopularFilms(
            @RequestParam(value = "count", defaultValue = "10") @Positive Integer count,
            @RequestParam(value = "genreId", required = false) Integer genreId,
            @RequestParam(value = "year", required = false) Integer year) {
        log.info("Запрос топ-{} популярных фильмов с фильтрацией по жанру={} и году={}", count, genreId, year);
        return filmService.findMostPopularFilms(count, genreId, year);
    }
}


