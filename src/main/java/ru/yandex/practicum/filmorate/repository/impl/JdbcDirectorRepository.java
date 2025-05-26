package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorRepository;
import ru.yandex.practicum.filmorate.repository.mappers.DirectorRowMapper;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
@Component
public class JdbcDirectorRepository implements DirectorRepository {

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper mapper;

    @Override
    public List<Director> findAll() {
        String query = "SELECT id, name FROM DIRECTORS";
        return jdbc.query(query, mapper);
    }

    @Override
    public Director findById(Long id) {
        String query = "SELECT id, name FROM DIRECTORS WHERE id = ?";
        try {
            List<Director> directors = jdbc.query(query, mapper, id);
            if (directors.isEmpty()) {
                return null;
            } else {
                return directors.get(0);
            }
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public Director save(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = "INSERT INTO DIRECTORS (NAME) VALUES ( ? )";

        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(query, new String[]{"id"});
            statement.setString(1, director.getName());
            return statement;
        }, keyHolder);

        long id = Objects.requireNonNull(keyHolder.getKey()).longValue();
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = "UPDATE DIRECTORS SET NAME = ? WHERE ID = ?";
        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, director.getName());
            statement.setLong(2, director.getId());
            return statement;
        }, keyHolder);

        return findById(director.getId());
    }

    @Override
    public void delete(Long id) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = "DELETE FROM DIRECTORS WHERE ID = ?";

        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, id);
            return statement;
        }, keyHolder);
    }

    @Override
    public Map<Long, Set<Director>> getDirectorsByFilmIds(Set<Long> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String sqlQuery = String.format("""
        SELECT fd.film_id, d.id AS director_id, d.name AS director_name
        FROM film_directors fd
        JOIN directors d ON fd.director_id = d.id
        WHERE fd.film_id IN (%s)
    """, filmIds.stream().map(id -> "?").collect(Collectors.joining(", ")));

        Object[] params = filmIds.toArray();

        List<Map.Entry<Long, Director>> result = jdbc.query(sqlQuery, params, (rs, rowNum) -> {
            Long filmId = rs.getLong("film_id");
            Long directorId = rs.getLong("director_id");
            String directorName = rs.getString("director_name");
            Director director = new Director(directorId, directorName);
            return Map.entry(filmId, director);
        });

        return result.stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toSet())
                ));
    }
}
