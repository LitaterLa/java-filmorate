package ru.yandex.practicum.filmorate.repository.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpaa;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmRowMapper {

    public Film mapFilm(ResultSet rs) throws SQLException {
        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("film_name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getInt("duration"))
                .build();
    }

    public Mpaa mapMpaa(ResultSet rs) throws SQLException {
        return new Mpaa(
                rs.getInt("rating_id"),
                rs.getString("rating_name")
        );
    }
}
