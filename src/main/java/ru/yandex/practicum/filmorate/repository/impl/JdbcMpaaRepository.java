package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpaa;
import ru.yandex.practicum.filmorate.repository.MpaaRepository;
import ru.yandex.practicum.filmorate.repository.mappers.MpaaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcMpaaRepository implements MpaaRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final MpaaRowMapper mapper;

    @Override
    public List<Mpaa> getAllRatings() {
        String query = "SELECT * FROM MPAA";
        return jdbc.query(query, mapper);
    }

    @Override
    public Optional<Mpaa> getById(int id) {
        String query = "SELECT * FROM MPAA where id = :id";
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        try {
            Mpaa mpaa = jdbc.queryForObject(query, params, mapper);
            return Optional.of(mpaa);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }
}
