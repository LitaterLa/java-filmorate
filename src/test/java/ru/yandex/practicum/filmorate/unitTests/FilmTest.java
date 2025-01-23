package ru.yandex.practicum.filmorate.unitTests;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpaa;
import ru.yandex.practicum.filmorate.validation.Create;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FilmTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidFilm() {
        Film film = new Film();
        film.setName("Valid Title");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 12, 12));
        film.setDuration(60);
        film.setMpa(new Mpaa());

        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void testInvalidDuration() {
        Film film = new Film();
        film.setName("Valid Title");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 12, 12));
        film.setDuration(-60);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidReleaseDate() {
        Film film = new Film();
        film.setName("Valid Title");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(1800, 12, 12));
        film.setDuration(60);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    public void testInvalidDescription() {
        Film film = new Film();
        film.setName("Valid Title");
        film.setDescription("SoInvalid".repeat(23)); // Over 200 characters
        film.setReleaseDate(LocalDate.of(2000, 12, 12));
        film.setDuration(60);

        Set<ConstraintViolation<Film>> violations = validator.validate(film, Create.class);
        assertFalse(violations.isEmpty());
    }
}
