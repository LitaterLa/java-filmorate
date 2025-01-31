package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.ReviewRepository;
import ru.yandex.practicum.filmorate.repository.mappers.ReviewRowMapper;

import java.sql.Types;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcReviewRepository implements ReviewRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final ReviewRowMapper mapper;

    @Override
    public Review save(Review review) {
        String queryWithUseful = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES (:content, :is_positive, :user_id, :film_id, :useful)";
        String queryWithoutUseful = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) " +
                "VALUES (:content, :is_positive, :user_id, :film_id, 0)";

        boolean hasUseful = review.getUseful() != null;
        String query = hasUseful ? queryWithUseful : queryWithoutUseful;

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("is_positive", review.getIsPositive())
                .addValue("user_id", review.getUserId())
                .addValue("film_id", review.getFilmId());

        if (hasUseful) {
            params.addValue("useful", review.getUseful());
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(query, params, keyHolder);

        review.setReviewId(Objects.requireNonNull(keyHolder.getKey().intValue()));
        review.setUseful(review.getUseful() != null ? review.getUseful() : 0); // Присваиваем 0 в объекте

        return review;
    }



    @Override
    public void delete(Integer id) {
        String query = "DELETE FROM reviews WHERE review_id = :id";
        jdbc.update(query, new MapSqlParameterSource().addValue("id", id));
    }

    @Override
    public Review update(Review review) {
        String query = "UPDATE reviews SET content = :content, is_positive = :is_positive, " +
                "user_id = :user_id, film_id = :film_id, useful = :useful " +
                "WHERE review_id = :review_id";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("review_id", review.getReviewId())
                .addValue("user_id", review.getUserId())
                .addValue("film_id", review.getFilmId())
                .addValue("is_positive", review.getIsPositive(), Types.BOOLEAN)
                .addValue("content", review.getContent())
                .addValue("useful", review.getUseful() != null ? review.getUseful() : 0);

        jdbc.update(query, params);
        return review;
    }

    @Override
    public Optional<Review> getById(Integer id) {
        String query = "SELECT review_id, content, is_positive, user_id, film_id, useful " +
                "FROM reviews WHERE review_id = :id";
        try {
            Review review = jdbc.queryForObject(query, new MapSqlParameterSource().addValue("id", id), mapper);
            return Optional.of(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getReviewsByFilmId(Optional<Long> filmId, Integer count) {
        String query = "SELECT review_id, content, is_positive, user_id, film_id, useful " +
                "FROM reviews " +
                (filmId.isPresent() ? "WHERE film_id = :filmId " : "") +
                "ORDER BY useful DESC " +
                "LIMIT :count";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("count", count);
        filmId.ifPresent(id -> params.addValue("filmId", id));

        return jdbc.query(query, params, mapper);
    }

    @Override
    public void addLike(Integer reviewId, Long userId) {
        String checkQuery = "SELECT COUNT(*) FROM review_reactions WHERE review_id = :review_id AND user_id = :user_id";
        Integer count = jdbc.queryForObject(checkQuery, new MapSqlParameterSource()
                .addValue("review_id", reviewId)
                .addValue("user_id", userId), Integer.class);

        if (count > 0) {
            String updateQuery = "UPDATE review_reactions SET is_like = true WHERE review_id = :review_id AND user_id = :user_id";
            jdbc.update(updateQuery, new MapSqlParameterSource()
                    .addValue("review_id", reviewId)
                    .addValue("user_id", userId));
        } else {
            String insertQuery = "INSERT INTO review_reactions (review_id, user_id, is_like) VALUES (:review_id, :user_id, true)";
            jdbc.update(insertQuery, new MapSqlParameterSource()
                    .addValue("review_id", reviewId)
                    .addValue("user_id", userId));
        }

        String queryReview = "UPDATE reviews SET useful = useful + 1 WHERE review_id = :review_id";
        jdbc.update(queryReview, new MapSqlParameterSource().addValue("review_id", reviewId));
    }


    @Override
    public void removeLike(Integer reviewId, Long userId) {
        String query = "DELETE FROM review_reactions WHERE review_id = :review_id AND user_id = :user_id AND is_like = true";
        jdbc.update(query, new MapSqlParameterSource()
                .addValue("review_id", reviewId)
                .addValue("user_id", userId));

        String queryReview = "UPDATE reviews SET useful = useful - 1 WHERE review_id = :review_id";
        jdbc.update(queryReview, new MapSqlParameterSource().addValue("review_id", reviewId));
    }

    @Override
    public void addDislike(Integer reviewId, Long userId) {
        String checkQuery = "SELECT COUNT(*) FROM review_reactions WHERE review_id = :review_id AND user_id = :user_id";
        Integer count = jdbc.queryForObject(checkQuery, new MapSqlParameterSource()
                .addValue("review_id", reviewId)
                .addValue("user_id", userId), Integer.class);

        if (count > 0) {
            String updateQuery = "UPDATE review_reactions SET is_like = false WHERE review_id = :review_id AND user_id = :user_id";
            jdbc.update(updateQuery, new MapSqlParameterSource()
                    .addValue("review_id", reviewId)
                    .addValue("user_id", userId));
        } else {
            String insertQuery = "INSERT INTO review_reactions (review_id, user_id, is_like) VALUES (:review_id, :user_id, false)";
            jdbc.update(insertQuery, new MapSqlParameterSource()
                    .addValue("review_id", reviewId)
                    .addValue("user_id", userId));
        }

        String queryReview = "UPDATE reviews SET useful = useful - 1 WHERE review_id = :review_id";
        jdbc.update(queryReview, new MapSqlParameterSource().addValue("review_id", reviewId));
    }

    @Override
    public void removeDislike(Integer reviewId, Long userId) {
        String deleteQuery = "DELETE FROM review_reactions WHERE review_id = :review_id AND user_id = :user_id AND is_like = false";
        jdbc.update(deleteQuery, new MapSqlParameterSource()
                .addValue("review_id", reviewId)
                .addValue("user_id", userId));

        String queryReview = "UPDATE reviews SET useful = useful + 1 WHERE review_id = :review_id";
        jdbc.update(queryReview, new MapSqlParameterSource().addValue("review_id", reviewId));
    }

}
