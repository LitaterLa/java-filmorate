package ru.yandex.practicum.filmorate.repository.mappers;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpaa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<Genre> mapGenres(ResultSet rs) throws SQLException {
        String genresStr = rs.getString("genres");
        if (genresStr != null) {
            return Arrays.stream(genresStr.split(","))
                    .map(g -> {
                        String[] parts = g.split(":");
                        return new Genre(Integer.parseInt(parts[0]), parts[1]);
                    })
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public List<Director> mapDirectors(ResultSet rs) throws SQLException {
        String directorsStr = rs.getString("directors");
        if (directorsStr != null) {
            return Arrays.stream(directorsStr.split(","))
                    .map(d -> {
                        String[] parts = d.split(":");
                        if (parts.length == 2) {
                            try {
                                Long id = Long.valueOf(parts[0].trim());
                                String name = parts[1].trim();
                                return new Director(id, name);
                            } catch (NumberFormatException e) {
                                throw new IllegalArgumentException("Invalid director ID format: " + parts[0], e);
                            }
                        } else {
                            throw new IllegalArgumentException("Invalid director format: " + d);
                        }
                    })
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
