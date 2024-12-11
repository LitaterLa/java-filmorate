package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {
    @EqualsAndHashCode.Include
    @NotNull(groups = {Update.class})
    private Long id;

    @NotNull(groups = {Create.class, Update.class})
    @NotBlank(message = "Email не должен быть пустым")
    @Email()
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @NotNull(groups = {Create.class, Update.class})
    @Pattern(regexp = "^\\S+$", message = "Логин не может содержать пробелы")
    private String login;
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
    @PastOrPresent(message = "Неверная дата дня рождения")
    private LocalDate birthday;

    public User(String login, String name, String email, LocalDate birthday) {
        this.login = login;
        this.name = name;
        this.email = email;
        this.birthday = birthday;
    }
}