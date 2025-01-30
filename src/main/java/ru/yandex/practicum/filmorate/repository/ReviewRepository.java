package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository {
    Review save(Review review);

    void delete(Integer id);

    Review update(Review review);

    Optional<Review> getById(Integer id);

    List<Review> getReviewsByFilmId(Optional<Long> filmId, Integer count);

    void addLike(Integer reviewId, Long userId);

    void removeLike(Integer reviewId, Long userId);

    void addDislike(Integer reviewId, Long userId);

    void removeDislike(Integer reviewId, Long userId);
}
