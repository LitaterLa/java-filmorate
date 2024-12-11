package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class FilmTest {
    private static Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.usingContext().getValidator();
    }

    @Test
    public void testValidFilm() {
        Film film1 = new Film("Valid", "Valid", LocalDate.of(2000, 12, 12), Duration.of(60, ChronoUnit.MINUTES));
        Set<ConstraintViolation<Film>> violations = validator.validate(film1);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidDuration() {
        Film film = new Film(
                "Valid",
                "Valid",
                LocalDate.of(2000, 12, 12),
                Duration.ofMinutes(-60)
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidReleaseDate() {
        Film film = new Film(
                "Valid",
                "Valid",
                LocalDate.of(1800, 12, 12),
                Duration.ofMinutes(60)
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidDescription() {
        Film film = new Film(
                "Valid",
                "SoooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooInvalid",
                LocalDate.of(1800, 12, 12),
                Duration.ofMinutes(60)
        );

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty());
    }
}

