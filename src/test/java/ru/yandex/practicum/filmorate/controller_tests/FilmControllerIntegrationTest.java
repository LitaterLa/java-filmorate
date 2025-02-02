package ru.yandex.practicum.filmorate.controller_tests;

/*import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.BaseFilmService;
import ru.yandex.practicum.filmorate.service.UserServiceImpl;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FilmControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BaseFilmService filmService;

    @Autowired
    private UserServiceImpl userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setup() {
        user1 = userService.save(User.builder()
                .email("user1@example.com")
                .login("user1")
                .name("User One")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        user2 = userService.save(User.builder()
                .email("user2@example.com")
                .login("user2")
                .name("User Two")
                .birthday(LocalDate.of(1992, 2, 2))
                .build());

        Film film1 = filmService.save(Film.builder()
                .name("Film One")
                .description("Description One")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .mpa(new ru.yandex.practicum.filmorate.model.Mpaa(1, "G"))
                .build());

        Film film2 = filmService.save(Film.builder()
                .name("Film Two")
                .description("Description Two")
                .releaseDate(LocalDate.of(2005, 5, 5))
                .duration(150)
                .mpa(new ru.yandex.practicum.filmorate.model.Mpaa(2, "PG"))
                .build());

        filmService.addLike(film1.getId(), user1.getId());
        filmService.addLike(film1.getId(), user2.getId());
        filmService.addLike(film2.getId(), user2.getId());
    }

    @Test
    void shouldReturnCommonFilms() throws Exception {
        mockMvc.perform(get("/films/common")
                        .param("userId", user1.getId().toString())
                        .param("friendId", user2.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Film One"));
    }
}*/