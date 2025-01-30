package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.impl.JdbcReviewRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final JdbcReviewRepository repository;
    private final BaseFilmService filmService;
    private final UserServiceImpl userService;

    @Override
    public Review save(Review review) {
        userService.getUserById(review.getUserId());
        filmService.getFilmByIdOrThrow(review.getFilmId());
        return repository.save(review);
    }

    @Override
    public void delete(Integer id) {
        repository.delete(id);
    }

    @Override
    public Review update(Review review) {
        repository.getById(review.getId()).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
        return repository.update(review);
    }

    @Override
    public Review getById(Integer id) {
        return repository.getById(id).orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    @Override
    public List<Review> getReviewsByFilmId(Optional<Long> filmId, Integer count) {
        filmId.ifPresent(filmService::getFilmByIdOrThrow);
        return repository.getReviewsByFilmId(filmId, count);
    }

    @Override
    public void addLike(Integer reviewId, Long userId) {
        this.getById(reviewId);
        userService.getUserById(userId);
        repository.addLike(reviewId, userId);

    }

    @Override
    public void removeLike(Integer reviewId, Long userId) {
        this.getById(reviewId);
        userService.getUserById(userId);
        repository.removeLike(reviewId, userId);
    }

    @Override
    public void addDislike(Integer reviewId, Long userId) {
        this.getById(reviewId);
        userService.getUserById(userId);
        repository.addDislike(reviewId, userId);
    }

    @Override
    public void removeDislike(Integer reviewId, Long userId) {
        this.getById(reviewId);
        userService.getUserById(userId);
        repository.removeDislike(reviewId, userId);
    }
}
