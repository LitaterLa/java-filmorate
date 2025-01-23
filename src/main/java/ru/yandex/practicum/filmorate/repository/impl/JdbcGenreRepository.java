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
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class JdbcGenreRepository implements GenreRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final GenreRowMapper mapper;

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = :id";

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

}
