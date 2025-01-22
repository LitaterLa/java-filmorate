package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
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

//    @Override
//    public void setFilmGenre(List<Film> films) {
//        String query = "INSERT INTO films_genres (film_id, genre_id) " +
//                "VALUES (:film_id, :genre_id)";
//        for (Film film : films) {
//            for (Genre genre : film.getGenre()) {
//                MapSqlParameterSource params = new MapSqlParameterSource();
//                params.addValue("film_id", film.getId());
//                params.addValue("genre_id", genre.getId());
//                jdbc.update(query, params);
//            }
//        }
//    }
//
//    @Override
//    public List<Genre> loadFilmGenre(List<Film> films) {
//        List<Long> ids = films.stream().map(Film::getId).collect(Collectors.toList());
//        String query = "SELECT g.* FROM genres g " +
//                "JOIN films_genres fg ON g.id = fg.genre_id " +
//                "WHERE fg.film_id in (:ids)";
//        MapSqlParameterSource params = new MapSqlParameterSource();
//        params.addValue("ids", ids);
//        return jdbc.query(query, params, mapper);
//    }
//
//    @Override
//    public List<Genre> findGenresByFilmIds(List<Long> filmIds) {
//        String query = "SELECT g.* " +
//                "FROM genres g JOIN film_genres fg ON g.id=fg.genre_id " +
//                "WHERE fg.film_id IN (:filmIds);";
//        MapSqlParameterSource params = new MapSqlParameterSource();
//        params.addValue("filmIds", filmIds);
//        return jdbc.query(query, params, mapper);
//    }
//
//    @Override
//    public List<Genre> getByIds(List<Long> genreIds) {
//        String query = "SELECT * FROM genres WHERE id IN (:genreIds)";
//        MapSqlParameterSource params = new MapSqlParameterSource();
//        params.addValue("genreIds", genreIds);
//        return jdbc.query(query, params, mapper);
//    }

}
