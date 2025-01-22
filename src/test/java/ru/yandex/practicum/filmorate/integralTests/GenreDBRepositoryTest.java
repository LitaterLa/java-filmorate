package ru.yandex.practicum.filmorate.integralTests;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.JdbcGenreRepository;
import ru.yandex.practicum.filmorate.repository.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({JdbcGenreRepository.class, GenreRowMapper.class})
public class GenreDBRepositoryTest {
    private final JdbcGenreRepository genreRepository;


    @Test
    public void testGetById() {
        Optional<Genre> genre = genreRepository.getById(5);
        assertThat(genre).isPresent().hasValueSatisfying(genreIn -> assertThat(genreIn).hasFieldOrPropertyWithValue("id", 5));

    }

    @Test
    public void testGetAll() {
        List<Genre> all = genreRepository.getAll();
        assertThat(all).hasSize(8);

        assertThat(all).extracting(Genre::getName).containsExactlyInAnyOrder("comedy", "action", "adventrure", "detective", "thriller", "sci-fi", "horror", "documentary");

        assertThat(all).extracting(Genre::getId).containsExactlyInAnyOrder(1, 2, 3, 4, 5, 6, 7, 8);
    }

//    @Test
//    public void testSetFilmGenre() {
//        Genre comedy = genreRepository.getById(1).orElseThrow(() -> new NotFoundException("Genre has not been found"));
//        Genre action = genreRepository.getById(2).orElseThrow(() -> new NotFoundException("Genre has not been found"));
//        Genre thriller = genreRepository.getById(5).orElseThrow(() -> new NotFoundException("Genre has not been found"));
//        Genre sciFi = genreRepository.getById(6).orElseThrow(() -> new NotFoundException("Genre has not been found"));
//
//        List<Film> films = List.of(new Film("Comedy Movie", "A fun comedy film", LocalDate.of(2023, 6, 15), 90,
//                        new LinkedHashSet<>(Arrays.asList(comedy)), 1),  // ratingId = 1 (G)
//                new Film("Action Movie", "An intense action film", LocalDate.of(2022, 8, 22), 120, new LinkedHashSet<>(Arrays.asList(action)), 2),  // ratingId = 2 (PG)
//                new Film("Thriller Movie", "A gripping thriller", LocalDate.of(2024, 3, 10), 110, new LinkedHashSet<>(Arrays.asList(thriller)), 3),  // ratingId = 3 (PG-13)
//                new Film("Sci-Fi Movie", "A futuristic sci-fi movie", LocalDate.of(2021, 12, 5), 150, new LinkedHashSet<>(Arrays.asList(sciFi, action)), 4)  // ratingId = 4 (R)
//        );
//
//        genreRepository.setFilmGenre(films, );
//    }
//
//    @Test
//    public void testLoadFilmGenre() {
//
//    }
//
//    @Test
//    public void testFindGenresByFilmIds() {
//
//    }
//
//    @Test
//    public void getByIds() {
//
//    }


}
