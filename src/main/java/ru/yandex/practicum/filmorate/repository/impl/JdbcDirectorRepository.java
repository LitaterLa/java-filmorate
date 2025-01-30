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
import java.util.Collection;
import java.util.Objects;

@RequiredArgsConstructor
@Repository
@Component
public class JdbcDirectorRepository implements DirectorRepository {

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper mapper;

    @Override
    public Collection<Director> findAll() {
        String query = "SELECT id, name FROM DIRECTORS";
        return jdbc.query(query, mapper);
    }

    @Override
    public Director findById(Long id) {
        String query = "SELECT id, name FROM DIRECTORS WHERE id = ?";
        try {
            return jdbc.queryForObject(query, mapper, id);
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
}
