package ru.yandex.practicum.filmorate.model;

import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.ReleaseDate;
import ru.yandex.practicum.filmorate.validation.Update;

import java.time.LocalDate;
import java.util.LinkedHashSet;

/**
 * Film.
 */
@Data
@Builder
@AllArgsConstructor
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
    private LinkedHashSet<Genre> genres;
    @NotNull(groups = {Create.class, Update.class})
    @ManyToOne
    @JoinColumn(name = "rating_id", referencedColumnName = "id")
    private Mpaa mpa;

    public Film(String name, String description, LocalDate releaseDate, int duration, LinkedHashSet<Genre> genres, Mpaa mpa) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
        this.genres = genres;
        this.mpa = mpa;

    }

}
