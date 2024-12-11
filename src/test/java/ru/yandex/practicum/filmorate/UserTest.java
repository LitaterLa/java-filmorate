package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.Create;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class UserTest {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.usingContext().getValidator();
    }

    @Test
    void validateName() {
        User user1 = new User("floppy-poppy", "Poppy", "poppy@gmail.com", LocalDate.of(1999, 12, 12));
        Set<ConstraintViolation<User>> violations = validator.validate(user1, Create.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void failNameValidation() {
        User user1 = new User("", "Poppy", "poppy@gmail.com", LocalDate.of(1999, 12, 12));
        Set<ConstraintViolation<User>> violations = validator.validate(user1, Create.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void failBirthValidation() {
        User user1 = new User("poppy111", "Poppy", "poppy@gmail.com", LocalDate.of(2100, 12, 12));
        Set<ConstraintViolation<User>> violations = validator.validate(user1, Create.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void failEmailValidation() {
        User user1 = new User("poppy111", "Poppy", "poppygmailcom", LocalDate.of(2000, 12, 12));
        Set<ConstraintViolation<User>> violations = validator.validate(user1, Create.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void passEmailValidation() {
        User user1 = new User("poppy111", "Poppy", "poppy@gmail.com", LocalDate.of(2000, 12, 12));
        Set<ConstraintViolation<User>> violations = validator.validate(user1, Create.class);
        assertTrue(violations.isEmpty());
    }
}
