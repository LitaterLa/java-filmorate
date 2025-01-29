package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class JdbcFilmRepository implements FilmRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final GenreRowMapper genreMapper;
    private final DirectorRowMapper directorRowMapper;

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
                "WHERE fg.film_id IN (:filmIds) " +
                "ORDER BY fg.film_id, g.id";
        MapSqlParameterSource genresParams = new MapSqlParameterSource();
        genresParams.addValue("filmIds", filmIds);
        List<Map<String, Object>> genreResults = jdbc.queryForList(genreQuery, genresParams);

        Map<Long, Set<Genre>> mappedGenres = new HashMap<>();
        for (Map<String, Object> row : genreResults) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            int genreId = (int) row.get("genre_id");
            String genreName = (String) row.get("genre_name");

            mappedGenres.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(new Genre(genreId, genreName));
        }

        String directorQuery = "SELECT fd.film_id, fd.director_id, dir.name as director_name " +
                "FROM film_directors fd JOIN directors dir on fd.director_id = dir.id " +
                "WHERE fd.film_id IN (:filmIdList)";
        MapSqlParameterSource directorParams = new MapSqlParameterSource();
        directorParams.addValue("filmIdList", filmIds);
        List<Map<String, Object>> directorResults = jdbc.queryForList(directorQuery, directorParams);

        Map<Long, Set<Director>> mappedDirectors = new HashMap<>();
        for (Map<String, Object> row : directorResults) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            Long directorId = ((Number) row.get("director_id")).longValue();
            String directorName = (String) row.get("director_name");

            mappedDirectors.computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                    .add(new Director(directorId, directorName));
        }

        films.forEach(film -> {
            film.setGenres(new LinkedHashSet<>(mappedGenres.getOrDefault(film.getId(), Set.of())));

            if (film.getGenres() == null) {
                film.setGenres(new LinkedHashSet<>());
            }
            Mpaa filmRating = Optional.ofNullable(film.getMpa())
                    .map(Mpaa::getId)
                    .map(ratingId -> new Mpaa(ratingId, "Unknown"))
                    .orElse(new Mpaa(0, "Unknown"));
            film.setMpa(filmRating);

            film.setDirectors(new LinkedHashSet<>(mappedDirectors.getOrDefault(film.getId(), Set.of())));

            if (film.getDirectors() == null) {
                film.setDirectors(new LinkedHashSet<>());
            }
        });

        return films;
    }

    @Override
    public Optional<Film> get(Long id) {
        String query = "SELECT f.id AS film_id, f.name AS film_name, f.description AS film_description, " +
                "f.release_date AS film_release_date, f.duration AS film_duration, f.rating_id AS film_rating_id, " +
                "m.id AS rating_id, m.name AS rating_name " +
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

            String genreQuery = "SELECT g.id AS genre_id, g.name AS genre_name " +
                    "FROM genres g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = :filmId " +
                    "GROUP BY fg.genre_id " +
                    "ORDER BY fg.genre_id";

            List<Genre> genres = jdbc.query(genreQuery, new MapSqlParameterSource("filmId", id), genreMapper);

            film.setGenres(new LinkedHashSet<>(genres));

            String directorQuery = "SELECT fd.director_id as id, dir.name " +
                    "FROM film_directors fd JOIN directors dir on fd.director_id = dir.id " +
                    "WHERE fd.film_id IN (:filmId)";

            List<Director> directors = jdbc.query(directorQuery, new MapSqlParameterSource("filmId", id),
                    directorRowMapper);

            film.setDirectors(new LinkedHashSet<>(directors));

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
            throw new ValidationException("У фильма должен быть рейтинг");
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
        addDirectors(film.getId(), film.getDirectors());

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

        removeDirectorFilm(film.getId());
        addDirectors(film.getId(), film.getDirectors());

        return film;
    }

    @Override
    public void delete(Long id) {
        removeLikeFilm(id);
        removeGenreFilm(id);

        String sqlQuery = "DELETE FROM films WHERE id = :filmId";

        jdbc.update(sqlQuery, new MapSqlParameterSource("filmId", id));
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
                "JOIN MPAA m ON f.rating_id = m.id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, m.name " +
                "ORDER BY like_count DESC LIMIT :count";

        return jdbc.query(query, new MapSqlParameterSource("count", count), (rs, rowNum) -> {
            Film film = mapper.mapFilm(rs);
            Mpaa mpaa = mapper.mapMpaa(rs);
            film.setMpa(mpaa);
            return film;
        });
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
                "SELECT :filmId, id FROM genres WHERE id IN (:genreIds)  ORDER BY id";

        Set<Integer> genreIds = new LinkedHashSet<>();
        for (Genre genre : filmGenres) {
            genreIds.add(genre.getId());
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("genreIds", genreIds);

        jdbc.update(sqlQuery, params);
    }

    private void addDirectors(Long filmId, Collection<Director> filmDirectors) {
        if (filmDirectors == null || filmDirectors.isEmpty()) {
            return;
        }

        String query = "INSERT INTO film_directors (DIRECTOR_ID, FILM_ID) " +
                "SELECT id, :filmId FROM DIRECTORS WHERE ID IN (:directorIdList) ORDER BY ID";

        Set<Long> directorIdList = new LinkedHashSet<>();
        for (Director director : filmDirectors) {
            directorIdList.add(director.getId());
        }

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("filmId", filmId)
                .addValue("directorIdList", directorIdList);
        jdbc.update(query, params);
    }

    private void removeDirectorFilm(Long id) {
        String sqlQuery = "DELETE FROM FILM_DIRECTORS WHERE film_id = :filmId";
        jdbc.update(sqlQuery, new MapSqlParameterSource("filmId", id));
    }

    @Override
    public Collection<Film> findByDirector(Long directorId, String sortType) {
        String sortTypeDB;
        switch (sortType) {
            case "likes":
                sortTypeDB = "lk.likes desc";
                break;
            case "year":
                sortTypeDB = "f.RELEASE_DATE";
                break;
            default:
                sortTypeDB = "f.id";
                break;
        }
        String filmQuery = "SELECT f.id AS film_id, f.name AS film_name, f.description AS film_description, " +
                "f.release_date AS film_release_date, f.duration AS film_duration, f.rating_id AS film_rating_id, " +
                "m.id AS rating_id, m.name AS rating_name " +
                "FROM films f " +
                "LEFT JOIN MPAA m ON f.rating_id = m.id " +
                "LEFT JOIN (select count(l.USER_ID) as likes, l.FILM_ID from LIKES l group by FILM_ID) lk on f.ID = lk.FILM_ID " +
                "WHERE exists(SELECT 1 FROM FILM_DIRECTORS fd WHERE fd.DIRECTOR_ID = :directorId) " +
                "ORDER BY " + sortTypeDB;
        List<Film> films = jdbc.query(filmQuery, new MapSqlParameterSource()
                        .addValue("directorId", directorId),
                (rs, rowNum) -> {
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
                "WHERE fg.film_id IN (:filmIds) " +
                "ORDER BY fg.film_id, g.id";
        MapSqlParameterSource genresParams = new MapSqlParameterSource();
        genresParams.addValue("filmIds", filmIds);
        List<Map<String, Object>> genreResults = jdbc.queryForList(genreQuery, genresParams);

        Map<Long, Set<Genre>> mappedGenres = new HashMap<>();
        for (Map<String, Object> row : genreResults) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            int genreId = (int) row.get("genre_id");
            String genreName = (String) row.get("genre_name");

            mappedGenres.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(new Genre(genreId, genreName));
        }

        String directorQuery = "SELECT fd.film_id, fd.director_id, dir.name as director_name " +
                "FROM film_directors fd JOIN directors dir on fd.director_id = dir.id " +
                "WHERE fd.film_id IN (:filmIdList)";
        MapSqlParameterSource directorParams = new MapSqlParameterSource();
        directorParams.addValue("filmIdList", filmIds);
        List<Map<String, Object>> directorResults = jdbc.queryForList(directorQuery, directorParams);

        Map<Long, Set<Director>> mappedDirectors = new HashMap<>();
        for (Map<String, Object> row : directorResults) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            Long dirId = ((Number) row.get("director_id")).longValue();
            String directorName = (String) row.get("director_name");

            mappedDirectors.computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                    .add(new Director(dirId, directorName));
        }

        films.forEach(film -> {
            film.setGenres(new LinkedHashSet<>(mappedGenres.getOrDefault(film.getId(), Set.of())));

            if (film.getGenres() == null) {
                film.setGenres(new LinkedHashSet<>());
            }

            film.setDirectors(new LinkedHashSet<>(mappedDirectors.getOrDefault(film.getId(), Set.of())));

            if (film.getDirectors() == null) {
                film.setDirectors(new LinkedHashSet<>());
            }

            Mpaa filmRating = Optional.ofNullable(film.getMpa())
                    .map(Mpaa::getId)
                    .map(ratingId -> new Mpaa(ratingId, "Unknown"))
                    .orElse(new Mpaa(0, "Unknown"));
            film.setMpa(filmRating);
        });

        return films;
    }
}
