package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmRepository implements FilmRepository {
    private final Map<Long, Film> films = new HashMap<>();
    //keys-filmId, values usersId
    private final Map<Long, Set<Long>> usersLikes = new HashMap<>();
    private long id = 1;

    @Override
    public Film save(Film film) {
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
        usersLikes.computeIfAbsent(film.getId(), id -> new HashSet<>()).add(user.getId());
    }

    @Override
    public void removeLike(Film film, User user) {
        Set<Long> likes = usersLikes.get(film.getId());
        if (likes == null || !likes.remove(user.getId())) {
            throw new NotFoundException("Лайк пользователя с ID " + user.getId() + " для фильма " + film.getId() + " не найден.");
        }
    }


    @Override
    public void delete(Long id) {
        films.remove(id);
        usersLikes.remove(id);
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return films.get(film.getId());
    }

    @Override
    public List<Film> findBestLiked(Integer count) {
        int maxCount = Math.min(count, films.size());
        return films.values().stream()
                .sorted((f1, f2) -> Integer.compare(usersLikes.getOrDefault(f2.getId(), Set.of()).size(), usersLikes.getOrDefault(f1.getId(), Set.of()).size()))
                .limit(maxCount)
                .collect(Collectors.toList());

    }

    private long generateId() {
        return id++;
    }
}
