package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpaa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.MpaaRowMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class JdbcFilmRepository implements FilmRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final FilmRowMapper mapper;
    private final GenreRowMapper genreMapper;
    private final MpaaRowMapper ratingMapper;

    @Override
    public Film save(Film film) {
        String query = "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                "VALUES (:name, :description, :release_date, :duration, :rating_id)";

        Map<String, Object> params = new HashMap<>();
        params.put("name", film.getName());
        params.put("description", film.getDescription());
        params.put("release_date", film.getReleaseDate());
        params.put("duration", film.getDuration());
        params.put("rating_id", film.getRating() != null ? film.getRating().getId() : null);

        film.setId(insert(query, params));
        insertFilmGenres(film);
        return film;
    }

    @Override
    public void delete(Long id) {
        String deleteGenres = "DELETE FROM film_genres WHERE film_id = :id";
        jdbc.update(deleteGenres, new MapSqlParameterSource("id", id));

        String deleteFilm = "DELETE FROM films WHERE id = :id";
        jdbc.update(deleteFilm, new MapSqlParameterSource("id", id));
    }

    @Override
    public Film update(Film film) {
        String query = "UPDATE films " +
                "SET name = :name, description = :description, release_date = :release_date, duration = :duration, rating_id = :rating_id " +
                "WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("release_date", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("rating_id", film.getRating() != null ? film.getRating().getId() : null)
                .addValue("id", film.getId());
        jdbc.update(query, params);

        String delete = "DELETE FROM film_genres WHERE film_id = :film_id";
        jdbc.update(delete, new MapSqlParameterSource("film_id", film.getId()));
        insertFilmGenres(film);
        return film;
    }

    @Override
    public Optional<Film> get(Long id) {
        String query = "SELECT * FROM films WHERE id = :id";
        try {
            Film film = jdbc.queryForObject(query, new MapSqlParameterSource("id", id), mapper);
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Film> getAll() {
        String filmQuery = "SELECT * FROM films";
        List<Film> films = jdbc.query(filmQuery, mapper);
        if (films.isEmpty()) {
            return films;
        }
        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        String genreQuery = "SELECT * FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (:filmIds)";
        MapSqlParameterSource genresParams = new MapSqlParameterSource();
        genresParams.addValue("filmIds", filmIds);
        List<Genre> genres = jdbc.query(genreQuery, genresParams, genreMapper);

        Map<Long, List<Genre>> mappedGenres = genres.stream()
                .filter(genre -> genre.getFilms() != null && !genre.getFilms().isEmpty())
                .collect(Collectors.groupingBy(genre -> {
                    Set<Film> innerFilms = genre.getFilms();
                    if (innerFilms == null || innerFilms.isEmpty()) {
                        throw new IllegalStateException("Genre has no associated films: " + genre.getId());
                    }
                    return innerFilms.iterator().next().getId();
                }));

        String mpaaQuery = "SELECT * FROM Mpaa";
        List<Mpaa> ratings = jdbc.query(mpaaQuery, ratingMapper);

        Map<Integer, Mpaa> ratingMap = ratings.stream()
                .collect(Collectors.toMap(Mpaa::getId, rating -> rating));

        films.forEach(film -> {
            // Устанавливаем жанры
            film.setGenre(new LinkedHashSet<>(mappedGenres.getOrDefault(film.getId(), List.of())));

            // Проверяем и устанавливаем рейтинг
            Mpaa filmRating = ratingMap.get(film.getRating().getId()); // Получаем рейтинг по ID
            if (filmRating != null) {
                film.setRating(filmRating); // Устанавливаем найденный рейтинг
            } else {
                System.out.println("MPAA rating not found for rating_id: " + film.getRating().getId() + " in film: " + film.getId());
                film.setRating(new Mpaa(0, "Unknown")); // Устанавливаем рейтинг по умолчанию
            }
        });


        return films;
    }

    @Override
    public void addLike(Film film, User user) {
        String query = "INSERT INTO likes (user_id, film_id) VALUES (:user_id, :film_id)";
        jdbc.update(query, new MapSqlParameterSource("user_id", user.getId())
                .addValue("film_id", film.getId()));
    }

    @Override
    public void removeLike(Film film, User user) {
        String query = "DELETE FROM likes WHERE user_id = :user_id AND film_id = :film_id";
        jdbc.update(query, new MapSqlParameterSource("user_id", user.getId())
                .addValue("film_id", film.getId()));
    }

    @Override
    public List<Film> findBestLiked(Integer count) {
        String query = "SELECT " +
                "f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, " +
                "COUNT(l.user_id) AS count " +
                "FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY count DESC " +
                "LIMIT :count";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("count", count);
        return jdbc.query(query, params, mapper);
    }

    private Long insert(String query, Map<String, Object> params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(query, new MapSqlParameterSource(params), keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные фильма");
        }
    }

    private void insertFilmGenres(Film film) {
        String query = "INSERT INTO film_genres(film_id, genre_id) VALUES (:film_id, :genre_id)";

        List<MapSqlParameterSource> batchParams = new ArrayList<>();
        for (Genre genre : film.getGenre()) {
            MapSqlParameterSource params = new MapSqlParameterSource();
            params.addValue("film_id", film.getId());
            params.addValue("genre_id", genre.getId());
            batchParams.add(params);
        }
        jdbc.batchUpdate(query, batchParams.toArray(new MapSqlParameterSource[0]));
    }


    protected void update(String query, Map<String, Object> params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные фильма");
        }
    }
}

