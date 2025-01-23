package ru.yandex.practicum.filmorate.integralTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Mpaa;
import ru.yandex.practicum.filmorate.repository.impl.JdbcMpaaRepository;
import ru.yandex.practicum.filmorate.repository.mappers.MpaaRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({JdbcMpaaRepository.class, MpaaRowMapper.class})
public class MpaaDBRepositoryTest {
    private final JdbcMpaaRepository mpaaRepository;
    Mpaa ratingG = new Mpaa(1, "G");

    @Test
    public void testGetById() {
        Optional<Mpaa> optionalMpaa = mpaaRepository.getById(ratingG.getId());
        assertThat(optionalMpaa)
                .isPresent()
                .hasValueSatisfying(userIn ->
                        assertThat(userIn).hasFieldOrPropertyWithValue("id", ratingG.getId())
                );

    }

    @Test
    public void testGetAll() {
        List<Mpaa> ratings = mpaaRepository.getAllRatings();
        assertThat(ratings).hasSize(5);

        assertThat(ratings).extracting(Mpaa::getName).containsExactlyInAnyOrder(
                "G", "PG", "PG-13", "R", "NC-17"
        );

        assertThat(ratings).extracting(Mpaa::getId).containsExactlyInAnyOrder(
                1, 2, 3, 4, 5
        );


    }
}
