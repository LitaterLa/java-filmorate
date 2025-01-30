package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

/**
 * Review
 */
@Data
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class Review {

    @NotNull(groups = {Update.class})
    private Integer id;
    @NotBlank(message = "Поле не может быть пустым", groups = {Create.class, Update.class})
    private String content;
    @NotNull(groups = {Create.class, Update.class})
    private Boolean isPositive;
    @NotNull(groups = {Create.class, Update.class})
    private Long userId;
    @NotNull(groups = {Create.class, Update.class})
    private Long filmId;

    private Integer rate;
}
