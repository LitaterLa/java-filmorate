package ru.yandex.practicum.filmorate.integralTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpaa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.JdbcFilmRepository;
import ru.yandex.practicum.filmorate.repository.JdbcUserRepository;
import ru.yandex.practicum.filmorate.repository.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.MpaaRowMapper;
import ru.yandex.practicum.filmorate.repository.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({JdbcFilmRepository.class, FilmRowMapper.class, GenreRowMapper.class, UserRowMapper.class, MpaaRowMapper.class, JdbcUserRepository.class})
public class FilmDBRepositoryTest {
    private final JdbcFilmRepository filmRepository;
    private final JdbcUserRepository userRepository;
    private final NamedParameterJdbcOperations jdbc;

    private Mpaa gRating = new Mpaa(1, "G");
    private Mpaa pgRating = new Mpaa(2, "PG");
    private Mpaa pg13Rating = new Mpaa(3, "PG-13");
    private Mpaa rRating = new Mpaa(4, "R");

    private Genre comedy = new Genre(1, "comedia");
    private Genre action = new Genre(2, "azione");
    private Genre thriller = new Genre(5, "thriller");
    private Genre sciFi = new Genre(8, "scientifico");

    private Film film1 = new Film("Comedy Movie", "A comedy", LocalDate.of(2023, 6, 15), 90,
            new LinkedHashSet<>(Arrays.asList(comedy)), gRating);
    private Film film2 = new Film("Action Movie", "An action", LocalDate.of(2022, 8, 22), 120,
            new LinkedHashSet<>(Arrays.asList(action)), pgRating);
    private Film film3 = new Film("Thriller Movie", "A thriller", LocalDate.of(2024, 3, 10), 110,
            new LinkedHashSet<>(Arrays.asList(thriller)), pg13Rating);
    private Film film4 = new Film("Sci-Fi Movie", "A sci-fi", LocalDate.of(2021, 12, 5), 150,
            new LinkedHashSet<>(Arrays.asList(sciFi, action)), rRating);

    @Test
    public void testSave() {
        Film savedFilm = filmRepository.save(film1);
        assertThat(savedFilm.getId()).isNotNull();
        assertThat(savedFilm)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedFilm);

    }

    @Test
    public void testDelete() {
        Film saved1 = filmRepository.save(film1);
        Film saved2 = filmRepository.save(film2);
        assertThat(filmRepository.getAll()).hasSize(2);
        filmRepository.delete(saved2.getId());
        assertThat(filmRepository.getAll()).hasSize(1).doesNotContain(saved2);
    }

    @Test
    public void testUpdate() {
        Film savedFilm = filmRepository.save(film1);
        savedFilm.setName("NewName");
        savedFilm.setDescription("newSample@email.com");
        savedFilm.setDuration(100);
        savedFilm.setRating(pgRating);
        savedFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmRepository.update(savedFilm);
        assertThat(savedFilm).isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(savedFilm);
    }


    @Test
    public void testFindById() {
        Film savedFilm = filmRepository.save(film1);
        Optional<Film> userOptional = filmRepository.get(savedFilm.getId());
        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(userIn ->
                        assertThat(userIn).hasFieldOrPropertyWithValue("id", savedFilm.getId())
                );

    }

    @Test
    public void testGetAll() {
        filmRepository.save(film1);
        filmRepository.save(film2);
        filmRepository.save(film3);
        filmRepository.save(film4);
        Collection<Film> films = filmRepository.getAll();
        assertThat(films).hasSize(4);

        assertThat(films).extracting(Film::getName).containsExactlyInAnyOrder(
                "Comedy Movie", "Action Movie", "Thriller Movie", "Sci-Fi Movie"
        );
    }

    @Test
    public void testAddLike() {
        Film savedFilm1 = filmRepository.save(film1);
        Film savedFilm2 = filmRepository.save(film2);
        User user = new User("sampleLogin1", "Sample Name1", "sample1@email.com", LocalDate.of(1900, 1, 1));
        userRepository.save(user);
        filmRepository.addLike(savedFilm1, user);
        filmRepository.addLike(savedFilm2, user);

        String query = "SELECT COUNT(*) FROM likes WHERE user_id = :user_id AND film_id = :film_id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("user_id", user.getId())
                .addValue("film_id", savedFilm1.getId());
        int count = jdbc.queryForObject(query, params, Integer.class);
        assertThat(count).isEqualTo(1);

    }

    @Test
    public void testDeleteLike() {
        Film savedFilm1 = filmRepository.save(film1);
        User user = new User("sampleLogin1", "Sample Name1", "sample1@email.com", LocalDate.of(1900, 1, 1));
        userRepository.save(user);
        filmRepository.addLike(savedFilm1, user);
        filmRepository.removeLike(savedFilm1, user);

        String query = "SELECT COUNT(*) FROM likes WHERE user_id = :user_id AND film_id = :film_id";
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("user_id", user.getId())
                .addValue("film_id", savedFilm1.getId());
        int count = jdbc.queryForObject(query, params, Integer.class);
        assertThat(count).isEqualTo(0);

    }

    @Test
    public void testFindBestLiked() {
        filmRepository.save(film1);
        filmRepository.save(film2);

        User user1 = new User("Login1", "User1", "user1@example.com", LocalDate.of(1990, 1, 1));
        User user2 = new User("Login2", "User2", "user2@example.com", LocalDate.of(1992, 1, 1));
        userRepository.save(user1);
        userRepository.save(user2);

        filmRepository.addLike(film1, user1);
        filmRepository.addLike(film1, user2);
        filmRepository.addLike(film2, user1);

        List<Film> bestLikedFilms = filmRepository.findBestLiked(2);

        assertThat(bestLikedFilms).hasSize(2);
        assertThat(bestLikedFilms.get(0).getName()).isEqualTo("Comedy Movie");
        assertThat(bestLikedFilms.get(1).getName()).isEqualTo("Action Movie");
    }


}

