package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Director {

    Long id;
    @NotEmpty
    String name;
}
