package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpaa;
import ru.yandex.practicum.filmorate.repository.SearchRepository;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;


@Repository
@RequiredArgsConstructor
public class SearchRepositoryImpl implements SearchRepository {
    private final NamedParameterJdbcTemplate jdbc;
    private final FilmRowMapper filmMapper;

    @Override
    public List<Film> searchFilm(String query, String searchBy) {
        String searchQuery = "SELECT " +
                "f.id AS film_id, " +
                "f.name AS film_name, " +
                "f.description AS film_description, " +
                "f.release_date AS film_release_date, " +
                "f.duration AS film_duration, " +
                "f.rating_id AS film_rating_id, " +
                "m.id AS mpa_id, " +
                "m.name AS mpa_name, " +
                "g.id AS genre_id, " +
                "g.name AS genre_name, " +
                "d.id AS director_id, " +
                "d.name AS director_name, " +
                "(SELECT COUNT(*) FROM likes l WHERE l.film_id = f.id) AS film_likes " +
                "FROM FILMS f " +
                "LEFT JOIN MPAA m ON f.rating_id = m.id " +
                "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                "LEFT JOIN genres g ON fg.genre_id = g.id " +
                "LEFT JOIN film_directors fd ON f.id = fd.film_id " +
                "LEFT JOIN directors d ON fd.director_id = d.id " +
                validateWhereRequest(query, searchBy) +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, m.id, m.name, g.id, g.name, d.id, d.name " +
                "ORDER BY film_likes DESC";

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("query", "%" + query.toLowerCase() + "%");

        Map<Long, Film> resultingFilms = new HashMap<>();
        jdbc.query(searchQuery, params, rs -> {
            Long filmId = rs.getLong("film_id");
            Film film = resultingFilms.get(filmId);
            if (film == null) {
                film = filmMapper.mapFilm(rs);
                Mpaa mpaa = new Mpaa();
                mpaa.setId(rs.getInt("mpa_id"));
                mpaa.setName(rs.getString("mpa_name"));
                film.setMpa(mpaa);
                film.setGenres(new LinkedHashSet<>());
                film.setDirectors(new LinkedHashSet<>());
                resultingFilms.put(filmId, film);
            }
            Integer genreId = rs.getObject("genre_id", Integer.class);
            if (genreId != null) {
                String genreName = rs.getString("genre_name");
                Genre genre = new Genre(genreId, genreName);
                film.getGenres().add(genre);
            }
            Long directorId = rs.getObject("director_id", Long.class);
            if (directorId != null) {
                String directorName = rs.getString("director_name");
                Director director = new Director(directorId, directorName);
                film.getDirectors().add(director);
            }
        });
        return new ArrayList<>(resultingFilms.values());
//        return new ArrayList<>(resultingFilms.values().stream().sorted(Comparator.comparing(Film::getId)
//                        .reversed())
//                .collect(Collectors.toList()));
    }

    private String validateWhereRequest(String query, String searchBy) {
        List<String> searchParams = List.of(searchBy.split(","));
        if (searchParams.size() == 1) {
            if (searchParams.get(0).equalsIgnoreCase("director")) {
                return "WHERE LOWER(d.name) LIKE :query ";
            } else if (searchParams.get(0).equalsIgnoreCase("title")) {
                return "WHERE LOWER(f.name) LIKE :query ";
            }
        } else if (searchParams.size() == 2) {
            return "WHERE LOWER(d.name) LIKE :query OR LOWER(f.name) LIKE :query ";
        } else {
            throw new ValidationException("Invalid parameters");
        }
        return "";
    }
}
