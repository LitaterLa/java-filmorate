package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorRepository;
import ru.yandex.practicum.filmorate.repository.mappers.DirectorRowMapper;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Repository
public class JdbcDirectorRepository implements DirectorRepository {

    private final NamedParameterJdbcOperations jdbc;
    private final DirectorRowMapper mapper;

    @Override
    public Collection<Director> findAll() {
        String query = "SELECT id, name FROM DIRECTORS";
        return jdbc.query(query, mapper);
    }

    @Override
    public Director findById(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        String query = "SELECT id, name FROM DIRECTORS WHERE id = :id";
        try {
            return jdbc.queryForObject(query, params, mapper);
        } catch (EmptyResultDataAccessException ignored) {
            return null;
        }
    }

    @Override
    public Director save(Director director) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", director.getName());
        String query = "INSERT INTO DIRECTORS (NAME) VALUES (:name)";

        long id = jdbc.update(query, params);
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director director) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", director.getName());
        params.put("id", director.getId());
        String query = "UPDATE DIRECTORS SET NAME = :name WHERE ID = :id";

        jdbc.update(query, params);
        return director;
    }

    @Override
    public void delete(Long id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        String query = "DELETE FROM DIRECTORS WHERE ID = :id";

        jdbc.update(query, params);
    }
}
