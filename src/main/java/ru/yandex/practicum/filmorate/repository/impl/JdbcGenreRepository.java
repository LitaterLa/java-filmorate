package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class JdbcGenreRepository implements GenreRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final GenreRowMapper mapper;

    private static final String FIND_ALL_QUERY = "SELECT id, name FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT id, name FROM genres WHERE id = :id";

    @Override
    public Optional<Genre> getById(long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        try {
            Genre genre = jdbc.queryForObject(FIND_BY_ID_QUERY, params, mapper);
            return Optional.of(genre);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public List<Genre> getAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    public List<Genre> getByIds(Set<Integer> genreIds) {
        String sqlQuery = "SELECT id, name FROM genres WHERE id IN (:genreIds)";
        return jdbc.query(sqlQuery, new MapSqlParameterSource("genreIds", genreIds), mapper);
    }

    public Map<Long, Set<Genre>> getGenresByFilmIds(Set<Long> filmIds) {
        String sqlQuery = "SELECT fg.film_id, g.id, g.name " +
                "FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (:filmIds)";

        return jdbc.query(sqlQuery, new MapSqlParameterSource("filmIds", filmIds), (rs, rowNum) -> {
                    long filmId = rs.getLong("film_id");
                    Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
                    return Map.entry(filmId, genre);
                }).stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));
    }
}
