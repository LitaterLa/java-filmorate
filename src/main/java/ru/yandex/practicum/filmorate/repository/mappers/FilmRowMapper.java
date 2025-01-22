package ru.yandex.practicum.filmorate.repository.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.RowMapper;
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
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        LinkedHashSet<Genre> genres = getFilmGenres(film.getId());
        film.setGenre(genres);
        Mpaa rating = getMpaaById(rs.getInt("rating_id"));
        film.setRating(rating);
        return film;
    }

    private Mpaa getMpaaById(int ratingId) {
        String query = "SELECT id, name FROM MPAA WHERE id = :ratingId";
        Map<String, Object> params = new HashMap<>();
        params.put("ratingId", ratingId);

        return jdbcTemplate.queryForObject(query, params, new MpaaRowMapper());
    }

    private LinkedHashSet<Genre> getFilmGenres(Long filmId) {
        String select = "SELECT g.id, g.name FROM genres g " +
                "JOIN film_genres fg ON fg.genre_id=g.id " +
                " WHERE fg.film_id = :filmId";

        Map<String, Object> params = new HashMap<>();
        params.put("filmId", filmId);

        LinkedHashSet<Genre> genres = new LinkedHashSet<>(jdbcTemplate.query(select, params, new GenreRowMapper()));
        return genres;
    }

}
