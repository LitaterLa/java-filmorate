package ru.yandex.practicum.filmorate.repository.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpaa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FilmRowMapper implements RowMapper<Film> {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        Film film = mapFilm(rs);
        Mpaa mpaa = getFilmMpaa(film.getId());
        film.setMpa(mpaa);

        film.setGenres(getFilmGenres(film.getId()));

        return film;
    }


    private Mpaa getFilmMpaa(Long filmId) {
        String select = "SELECT m.id AS rating_id, m.name AS rating_name FROM MPAA m " +
                "JOIN films f ON f.rating_id = m.id " +
                "WHERE f.id = :filmId";

        Map<String, Object> params = new HashMap<>();
        params.put("filmId", filmId);

        return jdbcTemplate.queryForObject(select, params, (rs, rowNum) ->
                new Mpaa(rs.getInt("rating_id"), rs.getString("rating_name"))
        );
    }


    private LinkedHashSet<Genre> getFilmGenres(Long filmId) {
        String select = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON fg.genre_id = g.id " +
                "WHERE fg.film_id = :filmId";

        MapSqlParameterSource params = new MapSqlParameterSource().addValue("filmId", filmId);

        return new LinkedHashSet<>(jdbcTemplate.query(select, params, new GenreRowMapper()));
    }


    private Film mapFilm(ResultSet rs) throws SQLException {
        return Film.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .build();
    }

}

