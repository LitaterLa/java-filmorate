package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmRepository;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.time.Duration;
import java.util.Collection;
import java.util.Optional;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmRepository filmRepository;

    public FilmController(FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film add(@Validated(Create.class) @RequestBody Film film) {
        long durationInMinutes = film.getDuration().toMinutes();
        Duration duration = Duration.ofSeconds(durationInMinutes);
        film.setDuration(duration);
        log.info("начало валидации фильма при создании {}", film);
        film.setId(filmRepository.generateId());
        log.info("создание фильма ID {} ", film.getId());
        filmRepository.save(film.getId(), film);
        return filmRepository.get(film.getId());
    }

    @GetMapping
    public Collection<Film> getAll() {
        return filmRepository.getAll();
    }

    @PutMapping
    public Film update(@Validated(Update.class) @RequestBody Film newFilm) {
        return Optional.ofNullable(filmRepository.get(newFilm.getId())).map(oldFilm -> {
            log.info("валидация фильма ID {} при обновлении", newFilm.getId());
            oldFilm.setDescription(newFilm.getDescription());
            oldFilm.setDuration(newFilm.getDuration());
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
            oldFilm.setName(newFilm.getName());
            log.info("обновление фильма ID {}", newFilm.getId());
            return oldFilm;
        }).orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

}

