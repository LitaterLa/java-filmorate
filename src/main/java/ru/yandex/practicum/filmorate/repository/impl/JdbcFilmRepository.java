package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JdbcFilmRepository implements FilmRepository {

    private final NamedParameterJdbcTemplate jdbc;
    private final JdbcTemplate jdbcTemplate;
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

        film.setId(null);

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

        String deleteFilmQuery = "DELETE FROM films WHERE id = :filmId";

        jdbc.update(deleteFilmQuery, new MapSqlParameterSource("filmId", id));
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
        KeyHolder keyHolder = new GeneratedKeyHolder();
        if (filmDirectors == null || filmDirectors.isEmpty()) {
            return;
        }

        String query = "INSERT INTO film_directors (DIRECTOR_ID, FILM_ID) " +
                "VALUES ( ?, ? )";
        for (Director director : filmDirectors) {
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setLong(1, director.getId());
                statement.setLong(2, filmId);
                return statement;
            }, keyHolder);
        }

    }

    private void removeDirectorFilm(Long id) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = "DELETE FROM FILM_DIRECTORS WHERE film_id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            return statement;
        }, keyHolder);
    }

    @Override
    public Collection<Film> findByDirector(Long directorId, String sortType) {
        String sortTypeDB = switch (sortType) {
            case "likes" -> "lk.likes desc";
            case "year" -> "f.RELEASE_DATE";
            default -> "f.id";
        };
        String filmQuery = "SELECT f.id AS film_id, f.name AS film_name, f.description AS film_description, " +
                "f.release_date AS film_release_date, f.duration AS film_duration, f.rating_id AS film_rating_id, " +
                "m.id AS rating_id, m.name AS rating_name " +
                "FROM films f " +
                "LEFT JOIN MPAA m ON f.rating_id = m.id " +
                "LEFT JOIN (select count(l.USER_ID) as likes, l.FILM_ID from LIKES l group by l.FILM_ID) lk on f.ID = lk.FILM_ID " +
                "WHERE f.ID in (Select fd.film_id from FILM_DIRECTORS fd WHERE fd.DIRECTOR_ID = :directorId) " +
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

    @Override
    public List<Film> findCommonFilms(Long userId, Long friendId) {
        String query = """
                SELECT f.id AS film_id, f.name AS film_name, f.description AS film_description,
                       f.release_date AS film_release_date, f.duration AS film_duration,
                       f.rating_id AS film_rating_id, m.name AS rating_name, COUNT(l.user_id) AS like_count
                FROM films f
                LEFT JOIN likes l ON f.id = l.film_id
                JOIN MPAA m ON f.rating_id = m.id
                WHERE f.id IN (
                    SELECT l1.film_id
                    FROM likes l1
                    JOIN likes l2 ON l1.film_id = l2.film_id
                    WHERE l1.user_id = :userId AND l2.user_id = :friendId
                )
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, m.name
                ORDER BY like_count DESC
                """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("userId", userId)
                .addValue("friendId", friendId);

        List<Film> films = jdbc.query(query, params, (rs, rowNum) -> {
            Film film = mapper.mapFilm(rs);
            Mpaa mpaa = mapper.mapMpaa(rs);
            film.setMpa(mpaa);
            return film;
        });
        if (!films.isEmpty()) {
            List<Long> filmIds = films.stream().map(Film::getId).toList();

            String genreQuery = """
                    SELECT fg.film_id AS film_id, g.id AS genre_id, g.name AS genre_name
                    FROM film_genres fg
                    JOIN genres g ON fg.genre_id = g.id
                    WHERE fg.film_id IN (:filmIds)
                    """;

            MapSqlParameterSource genreParams = new MapSqlParameterSource("filmIds", filmIds);
            List<Map<String, Object>> genreResults = jdbc.queryForList(genreQuery, genreParams);

            Map<Long, Set<Genre>> genresByFilm = new HashMap<>();
            for (Map<String, Object> row : genreResults) {
                Long filmId = ((Number) row.get("film_id")).longValue();
                int genreId = (int) row.get("genre_id");
                String genreName = (String) row.get("genre_name");

                genresByFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>())
                        .add(new Genre(genreId, genreName));
            }

            films.forEach(film -> film.setGenres(new LinkedHashSet<>(genresByFilm.getOrDefault(film.getId(), Set.of()))));
        }

        return films;
    }

    public Collection<Film> findFilmByUserLikes(Long userId) {
        String filmQuery = "SELECT f.id AS film_id, f.name AS film_name, f.description AS film_description, " +
                "f.release_date AS film_release_date, f.duration AS film_duration, f.rating_id AS film_rating_id, " +
                "m.id AS rating_id, m.name AS rating_name " +
                "FROM films f " +
                "LEFT JOIN MPAA m ON f.rating_id = m.id " +
                "WHERE f.id IN (SELECT l.film_id FROM LIKES l " +
                "WHERE l.USER_ID IN (SELECT l2.USER_ID FROM LIKES l1 JOIN LIKES l2 ON l1.FILM_ID = l2.FILM_ID " +
                "WHERE l1.USER_ID = :userId AND l2.USER_ID <> :userId GROUP BY l2.USER_ID) " +
                " AND l.FILM_ID NOT IN (SELECT lk.FILM_ID FROM LIKES lk WHERE lk.USER_ID = :userId))";

        return jdbc.query(filmQuery, new MapSqlParameterSource().addValue("userId", userId),
                (rs, rowNum) -> {
                    Film film = mapper.mapFilm(rs);
                    Mpaa mpaa = mapper.mapMpaa(rs);
                    film.setMpa(mpaa);
                    return film;
                });
    }

    @Override
    public List<Film> findMostPopularFilms(Integer count, Integer genreId, Integer year) {
        String sql = """
        SELECT f.id, f.name AS film_name, f.description, f.release_date, f.duration, f.rating_id,
               COALESCE(like_count, 0) AS likes, m.id AS rating_id, m.name AS rating_name
        FROM films f
        LEFT JOIN (
            SELECT film_id, COUNT(user_id) AS like_count
            FROM likes
            GROUP BY film_id
        ) l ON f.id = l.film_id
        LEFT JOIN MPAA m ON f.rating_id = m.id
        LEFT JOIN film_genres fg ON f.id = fg.film_id
        LEFT JOIN film_directors fd ON f.id = fd.film_id
        LEFT JOIN directors d ON fd.director_id = d.id
        WHERE (COALESCE(:genreId, -1) = -1 OR fg.genre_id = :genreId)
        AND (COALESCE(:year, -1) = -1 OR EXTRACT(YEAR FROM f.release_date) = :year)
        GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, m.id, m.name
        ORDER BY likes DESC
        LIMIT :count
    """;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("count", count)
                .addValue("genreId", genreId)
                .addValue("year", year);

        return jdbc.query(sql, params, (rs, rowNum) -> {
            Film film = mapper.mapFilm(rs);
            Mpaa mpaa = mapper.mapMpaa(rs);
            film.setMpa(mpaa);
            return film;
        });
    }
}
