package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.ReleaseDate;
import ru.yandex.practicum.filmorate.validation.Update;

import java.time.LocalDate;

/**
 * Film.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)

public class Film {
    @EqualsAndHashCode.Include
    @NotNull(groups = {Update.class})
    private Long id;
    @NotNull(groups = {Create.class, Update.class})
    @NotBlank(message = "Поле не может быть пустым", groups = {Create.class, Update.class})
    private String name;
    @NotNull(groups = {Create.class, Update.class})
    @NotBlank(groups = {Create.class, Update.class})
    @Size(max = 200, groups = {Create.class, Update.class})
    private String description;
    @NotNull(groups = {Create.class, Update.class})
    @ReleaseDate(groups = {Create.class, Update.class})
    private LocalDate releaseDate;

    @NotNull(groups = {Create.class, Update.class})
    @Positive(groups = {Create.class, Update.class})
    private int duration;


    public Film(String name, String description, LocalDate releaseDate, int duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }
}
