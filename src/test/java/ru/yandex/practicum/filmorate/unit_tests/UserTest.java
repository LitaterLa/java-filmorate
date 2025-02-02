package ru.yandex.practicum.filmorate.unit_tests;

/*import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.Create;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validateName() {
        User user = new User();
        user.setLogin("floppy-poppy");
        user.setName("Poppy");
        user.setEmail("poppy@gmail.com");
        user.setBirthday(LocalDate.of(1999, 12, 12));

        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class);
        assertTrue(violations.isEmpty());
    }

    @Test
    void failNameValidation() {
        User user = new User();
        user.setLogin("");
        user.setName("Poppy");
        user.setEmail("poppy@gmail.com");
        user.setBirthday(LocalDate.of(1999, 12, 12));

        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void failBirthValidation() {
        User user = new User();
        user.setLogin("poppy111");
        user.setName("Poppy");
        user.setEmail("poppy@gmail.com");
        user.setBirthday(LocalDate.of(2100, 12, 12));

        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void failEmailValidation() {
        User user = new User();
        user.setLogin("poppy111");
        user.setName("Poppy");
        user.setEmail("poppygmailcom");
        user.setBirthday(LocalDate.of(2000, 12, 12));

        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class);
        assertFalse(violations.isEmpty());
    }

    @Test
    void passEmailValidation() {
        User user = new User();
        user.setLogin("poppy111");
        user.setName("Poppy");
        user.setEmail("poppy@gmail.com");
        user.setBirthday(LocalDate.of(2000, 12, 12));

        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class);
        assertTrue(violations.isEmpty());
    }
}*/
