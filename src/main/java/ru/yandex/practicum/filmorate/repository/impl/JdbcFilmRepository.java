package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpaa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JdbcFilmRepository implements FilmRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final FilmRowMapper mapper;

    @Override
    public Collection<Film> getAll() {
        String filmQuery = "SELECT f.id AS film_id, f.name AS film_name, f.description AS film_description, " +
                "f.release_date AS film_release_date, f.duration AS film_duration, f.rating_id AS film_rating_id, " +
                "m.id AS rating_id, m.name AS rating_name " +
                "FROM films f " +
                "LEFT JOIN MPAA m ON f.rating_id = m.id";
        List<Film> films = jdbc.query(filmQuery, (rs, rowNum) -> {
            Film film = mapper.mapFilm(rs);
            Mpaa mpaa = mapper.mapMpaa(rs);
            film.setMpa(mpaa);
            return film;
        });

        if (films.isEmpty()) {
            return films;
        }

        List<Long> filmIds = films.stream().map(Film::getId).collect(Collectors.toList());

        String genreQuery = "SELECT fg.film_id AS film_id, g.id AS genre_id, g.name AS genre_name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (:filmIds)";
        MapSqlParameterSource genresParams = new MapSqlParameterSource();
        genresParams.addValue("filmIds", filmIds);
        List<Map<String, Object>> genreResults = jdbc.queryForList(genreQuery, genresParams);

        Map<Long, List<Genre>> mappedGenres = new HashMap<>();
        for (Map<String, Object> row : genreResults) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            int genreId = (int) row.get("genre_id");
            String genreName = (String) row.get("genre_name");

            mappedGenres.computeIfAbsent(filmId, k -> new ArrayList<>()).add(new Genre(genreId, genreName));
        }

        films.forEach(film -> {
            film.setGenres(new LinkedHashSet<>(mappedGenres.getOrDefault(film.getId(), List.of())));

            if (film.getGenres() == null) {
                film.setGenres(new LinkedHashSet<>());
            }
            Mpaa filmRating = Optional.ofNullable(film.getMpa())
                    .map(Mpaa::getId)
                    .map(ratingId -> new Mpaa(ratingId, "Unknown"))
                    .orElse(new Mpaa(0, "Unknown"));
            film.setMpa(filmRating);
        });

        return films;
    }

    @Override
    public Optional<Film> get(Long id) {
        String query = "SELECT f.id AS film_id, f.name AS film_name, f.description AS film_description, " +
                "f.release_date AS film_release_date, f.duration AS film_duration, " +
                "f.rating_id AS film_rating_id, m.id AS rating_id, m.name AS rating_name " +
                "FROM films f " +
                "LEFT JOIN MPAA m ON f.rating_id = m.id " +
                "WHERE f.id = :id";
        try {
            Film film = jdbc.queryForObject(query, new MapSqlParameterSource("id", id), (rs, rowNum) -> {
                Film f = mapper.mapFilm(rs);
                Mpaa mpaa = mapper.mapMpaa(rs);
                f.setMpa(mpaa);
                return f;
            });
            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Film save(Film film) {
        if (film.getGenres() == null) {
            film.setGenres(new LinkedHashSet<>());
        }
        if (film.getMpa() == null) {
            throw new ValidationException("Film must have a rating.");
        }

        String sqlQuery = "INSERT INTO films (name, description, release_date, duration, rating_id) " +
                "VALUES (:name, :description, :releaseDate, :duration, :ratingId)";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("ratingId", film.getMpa().getId());

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(sqlQuery, params, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        addGenres(film.getId(), film.getGenres());

        return film;
    }

    @Override
    public Film update(Film film) {
        if (film.getMpa() == null) {
            throw new ValidationException("Film must have a rating.");
        }
        String sqlQuery = "UPDATE films SET name = :name, description = :description, " +
                "release_date = :releaseDate, duration = :duration, rating_id = :ratingId " +
                "WHERE id = :id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("name", film.getName())
                .addValue("description", film.getDescription())
                .addValue("releaseDate", film.getReleaseDate())
                .addValue("duration", film.getDuration())
                .addValue("ratingId", film.getMpa().getId())
                .addValue("id", film.getId());

        jdbc.update(sqlQuery, params);

        removeGenreFilm(film.getId());
        addGenres(film.getId(), film.getGenres());

        return film;
    }

    @Override
    public void delete(Long id) {
        removeLikeFilm(id);
        removeGenreFilm(id);

        String sqlQuery = "DELETE FROM films WHERE id = :filmId";

        jdbc.update(sqlQuery, new MapSqlParameterSource("filmId", id));
    }

    private void removeLikeFilm(Long id) {
        String sqlQuery = "DELETE FROM likes WHERE film_id = :filmId";

        jdbc.update(sqlQuery, new MapSqlParameterSource("filmId", id));
    }

    private void removeGenreFilm(Long id) {
        String sqlQuery = "DELETE FROM film_genres WHERE film_id = :filmId";
        jdbc.update(sqlQuery, new MapSqlParameterSource("filmId", id));
    }

    private void addGenres(Long filmId, Collection<Genre> filmGenres) {
        if (filmGenres == null || filmGenres.isEmpty()) {
            return;
        }

        String sqlQuery = "INSERT INTO film_genres (film_id, genre_id) " +
                "SELECT :filmId, id FROM genres WHERE id IN (:genreIds)";

        List<Integer> genreIds = filmGenres.stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("genreIds", genreIds);

        jdbc.update(sqlQuery, params);
    }

    @Override
    public void addLike(Film film, User user) {
        String query = "INSERT INTO likes (user_id, film_id) VALUES (:userId, :filmId)";

        jdbc.update(query, new MapSqlParameterSource()
                .addValue("userId", user.getId())
                .addValue("filmId", film.getId()));
    }

    @Override
    public void removeLike(Film film, User user) {
        String query = "DELETE FROM likes WHERE user_id = :userId AND film_id = :filmId";

        jdbc.update(query, new MapSqlParameterSource()
                .addValue("userId", user.getId())
                .addValue("filmId", film.getId()));
    }

    @Override
    public List<Film> findBestLiked(Integer count) {
        String query = "SELECT f.id AS film_id, f.name AS film_name, f.description AS description, " +
                "f.release_date AS release_date, f.duration AS duration, f.rating_id AS rating_id, " +
                "m.name AS rating_name, COUNT(l.user_id) AS like_count " +
                "FROM films f LEFT JOIN likes l ON f.id = l.film_id " +
                "INNER JOIN MPAA m ON f.rating_id = m.id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, m.name " +
                "ORDER BY like_count DESC LIMIT :count";

        return jdbc.query(query, new MapSqlParameterSource("count", count), (rs, rowNum) -> {
            Film film = mapper.mapFilm(rs);
            Mpaa mpaa = mapper.mapMpaa(rs);
            film.setMpa(mpaa);
            return film;
        });
    }

}
