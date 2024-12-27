package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class InMemoryFilmRepository implements FilmRepository {
    private final Map<Long, Film> films = new HashMap<>();
    //keys-filmId, values usersId
    private final Map<Long, Set<Long>> usersLikes = new HashMap<>();
    private final UserRepository userRepository = new InMemoryUserRepository();
    private long id = 1;

    @Override
    public Film save(Film film) {
        if (film == null) {
            throw new ValidationException("Film cannot be null");
        }
        if (film.getId() == null) {
            film.setId(generateId());
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    @Override
    public Optional<Film> get(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLike(Film film, User user) {
        if (film == null || user == null) {
            throw new ValidationException("Data cannot be null");
        }
        if (films.get(film.getId()) == null || userRepository.get(user.getId()) == null) {
            throw new NotFoundException("Film or user not found");
        }
        usersLikes.computeIfAbsent(film.getId(), id -> new HashSet<>()).add(user.getId());
    }

    @Override
    public void removeLike(Film film, User user) {
        if (film == null || user == null) {
            throw new ValidationException("Data cannot be null");
        }
        if (films.get(film.getId()) == null || userRepository.get(user.getId()) == null) {
            throw new NotFoundException("Film or user not found");
        }
        Set<Long> likes = usersLikes.get(film.getId());
        if (likes == null || !likes.remove(user.getId())) {
            throw new NotFoundException("Лайк пользователя с ID " + user.getId() + " для фильма " + film.getId() + " не найден.");
        }
    }

    @Override
    public void delete(Long id) {
        if (films.get(id) == null) {
            throw new NotFoundException("Film not found");
        }
        films.remove(id);
        usersLikes.remove(id);
    }

    @Override
    public Film update(Film film) {
        if (film == null) {
            throw new ValidationException("Film to update is null");
        }
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        return films.get(film.getId());
    }

    @Override
    public Set<Long> getUsersLikes(Long filmId) {
        return usersLikes.getOrDefault(filmId, new HashSet<>());
    }

    private long generateId() {
        return id++;
    }

}
