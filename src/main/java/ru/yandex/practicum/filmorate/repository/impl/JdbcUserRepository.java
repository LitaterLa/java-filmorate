package ru.yandex.practicum.filmorate.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.util.*;

@Slf4j
@Repository
public class JdbcUserRepository implements UserRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final UserRowMapper mapper;
    private final JdbcTemplate jdbcTemplate;

    private static final String FIND_ALL_QUERY = "SELECT id, login, name, email, birthday FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT id, login, name, email, birthday FROM users WHERE id = :id";
    private static final String INSERT_QUERY = "INSERT INTO users(login, name, email, birthday)" +
            "VALUES (:login, :name, :email, :birthday)";
    private static final String UPDATE_QUERY = "UPDATE users SET name = :name, login = :login, email = :email, " +
            " birthday= :birthday WHERE id = :id";

    @Autowired
    public JdbcUserRepository(NamedParameterJdbcOperations jdbc, UserRowMapper mapper, JdbcTemplate jdbcTemplate) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User save(User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("login", user.getLogin());
        if (Objects.equals(user.getName(), "")) {
            user.setName(user.getLogin());
        }
        params.put("name", user.getName());
        params.put("email", user.getEmail());
        params.put("birthday", user.getBirthday());

        long id = insert(INSERT_QUERY, params);

        log.info("Inserted user: {}}", user);

        user.setId(id);
        return user;
    }

    @Override
    public void delete(Long id) {
        String q = "DELETE FROM USERS WHERE ID = ?";
        jdbcTemplate.update(q, id);
    }

    @Override
    public User update(User user) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", user.getId());
        params.put("login", user.getLogin());
        if (Objects.equals(user.getName(), "")) {
            user.setName(user.getLogin());
        }
        params.put("name", user.getName());
        params.put("email", user.getEmail());
        params.put("birthday", user.getBirthday());
        update(UPDATE_QUERY, params);

        return user;
    }

    @Override
    public Optional<User> get(Long id) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        try {
            User user = jdbc.queryForObject(FIND_BY_ID_QUERY, params, mapper);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getAll() {
        return jdbc.query(FIND_ALL_QUERY, mapper);
    }

    @Override
    public void addFriend(Long user, Long friend) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user1_id", user);
        params.addValue("user2_id", friend);
        jdbc.update("INSERT INTO friends(user1_id, user2_id)" +
                "VALUES(:user1_id, :user2_id)", params);
    }

    @Override
    public void deleteFriend(Long user, Long friend) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("user1_id", user);
        params.addValue("user2_id", friend);
        jdbc.update(
                "DELETE FROM friends WHERE user1_id = :user1_id AND user2_id = :user2_id",
                params
        );
    }

    @Override
    public Set<User> getFriends(Long userId) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        List<User> friends = jdbc.query(
                "SELECT u.* " +
                        "FROM friends f " +
                        "JOIN users u ON f.user2_id = u.id " +
                        "WHERE f.user1_id = :userId",
                params,
                mapper
        );
        return new HashSet<>(friends);
    }


    @Override
    public Set<User> getMutualFriends(Long id1, Long id2) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id1", id1);
        params.addValue("id2", id2);
        String query = "select u.* " +
                "from users u " +
                "where u.id in(select user2_id " +
                "from friends " +
                "where user1_id = :id1) " +
                "and u.id in (" +
                "select user2_id " +
                "from friends " +
                "where user1_id = :id2)";

        List<User> mutual = jdbc.query(query, params, mapper);
        return new HashSet<>(mutual);
    }

    protected Long insert(String query, Map<String, Object> params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(query, new MapSqlParameterSource(params), keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные пользователя!");
        }
    }


    protected void update(String query, Map<String, Object> params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные пользователя");
        }
    }

    protected void delete(String query, Long id) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        jdbc.update(query, params);
    }
}