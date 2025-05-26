package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;

public interface ReviewService {
    Review save(Review review);

    void delete(Integer id);

    Review update(Review review);

    Review getById(Integer id);

    List<Review> getReviewsByFilmId(Long filmId, Integer count);

    void addLike(Integer reviewId, Long userId);

    void removeLike(Integer reviewId, Long userId);

    void addDislike(Integer reviewId, Long userId);

    void removeDislike(Integer reviewId, Long userId);
}
