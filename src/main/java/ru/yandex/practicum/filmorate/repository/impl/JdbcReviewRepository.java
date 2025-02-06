package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.ReviewRepository;
import ru.yandex.practicum.filmorate.repository.mappers.ReviewRowMapper;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReviewRepository implements ReviewRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final JdbcTemplate jdbcTemplate;
    private final ReviewRowMapper mapper;

    @Override
    public Review save(Review review) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(query, new String[]{"review_id"});
            statement.setString(1, review.getContent());
            statement.setBoolean(2, review.getIsPositive());
            statement.setLong(3, review.getUserId());
            statement.setLong(4, review.getFilmId());
            statement.setLong(5, review.getUseful() != null ? review.getUseful() : 0);
            return statement;
        }, keyHolder);

        review.setReviewId((Integer) keyHolder.getKey());
        return review;
    }

    @Override
    public void delete(Integer id) {
        String query = "DELETE FROM reviews WHERE review_id = :id";
        jdbc.update(query, new MapSqlParameterSource().addValue("id", id));
    }

    public List<Review> getAllReviews() {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC";
        return jdbc.query(sql, mapper);
    }

    @Override
    public Review update(Review review) {
        String query = "UPDATE reviews SET content = ?, is_positive = ? " +
                "WHERE review_id = ?";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, review.getContent());
            statement.setBoolean(2, review.getIsPositive());
            statement.setLong(3, review.getReviewId());
            return statement;
        }, keyHolder);

        return getById(review.getReviewId()).orElseThrow();
    }

    @Override
    public Optional<Review> getById(Integer id) {
        String query = "SELECT review_id, content, is_positive, user_id, film_id, useful " +
                "FROM reviews WHERE review_id = :id";
        try {
            List<Review> reviews = jdbc.query(query, new MapSqlParameterSource().addValue("id", id), mapper);
            if (reviews.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(reviews.get(0));
            }
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, Integer count) {
        String query = "SELECT review_id, content, is_positive, user_id, film_id, useful " +
                "FROM reviews " +
                (filmId != null ? "WHERE film_id = :filmId " : "") +
                "ORDER BY USEFUL DESC " +
                "LIMIT :count";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("count", count);

        if (filmId != null) {
            params.addValue("filmId", filmId);
        }
        return jdbc.query(query, params, mapper);
    }

    @Override
    public void addLike(Integer reviewId, Long userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Review review = getById(reviewId).orElseThrow();
        if (!review.getIsPositive()) {
            String removeDislikeQuery = "DELETE FROM review_reactions " +
                    "WHERE review_id = ? AND user_id = ? AND is_like = false";
            int removedDislikes = jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(removeDislikeQuery);
                statement.setLong(1, reviewId);
                statement.setLong(2, userId);
                return statement;
            }, keyHolder);

            if (removedDislikes > 0) {
                String increaseUsefulQuery = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
                jdbcTemplate.update(connection -> {
                    PreparedStatement statement = connection.prepareStatement(increaseUsefulQuery);
                    statement.setLong(1, reviewId);
                    return statement;
                }, keyHolder);
            }
        }

        String insertLikeQuery = "INSERT INTO review_reactions (review_id, user_id, is_like) " +
                "VALUES (?, ?, true) ";
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(insertLikeQuery);
            statement.setLong(1, reviewId);
            statement.setLong(2, userId);
            return statement;
        }, keyHolder);

        String increaseUsefulQuery = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(increaseUsefulQuery);
            statement.setLong(1, reviewId);
            return statement;
        }, keyHolder);
    }


    @Override
    public void removeLike(Integer reviewId, Long userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ? AND is_like = true";
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setLong(1, reviewId);
            statement.setLong(2, userId);
            return statement;
        }, keyHolder);

        String queryReview = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(queryReview);
            statement.setLong(1, reviewId);
            return statement;
        }, keyHolder);
    }

    @Override
    public void addDislike(Integer reviewId, Long userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        Review review = getById(reviewId).orElseThrow();
        if (review.getIsPositive()) {
            String removeLikeQuery = "DELETE FROM review_reactions " +
                    "WHERE review_id = ? AND user_id = ? AND is_like = true";
            int removedLikes = jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(removeLikeQuery);
                statement.setLong(1, reviewId);
                statement.setLong(2, userId);
                return statement;
            }, keyHolder);

            if (removedLikes > 0) {
                String decreaseUsefulQuery = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
                jdbcTemplate.update(connection -> {
                    PreparedStatement statement = connection.prepareStatement(decreaseUsefulQuery);
                    statement.setLong(1, reviewId);
                    return statement;
                }, keyHolder);
            }
        }

        String insertDislikeQuery = "INSERT INTO review_reactions (review_id, user_id, is_like) " +
                "VALUES (?, ?, false)";
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(insertDislikeQuery);
            statement.setLong(1, reviewId);
            statement.setLong(2, userId);
            return statement;
        }, keyHolder);

        String decreaseUsefulQuery = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(decreaseUsefulQuery);
            statement.setLong(1, reviewId);
            return statement;
        }, keyHolder);
    }


    @Override
    public void removeDislike(Integer reviewId, Long userId) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String deleteQuery = "DELETE FROM review_reactions WHERE review_id = ? AND user_id = ? AND is_like = false";
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(deleteQuery);
            statement.setLong(1, reviewId);
            statement.setLong(2, userId);
            return statement;
        }, keyHolder);

        String queryReview = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(queryReview);
            statement.setLong(1, reviewId);
            return statement;
        }, keyHolder);
    }
}
