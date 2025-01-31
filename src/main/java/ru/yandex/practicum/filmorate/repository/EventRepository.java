package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.UserEvent;

import java.util.Collection;

public interface EventRepository {
    Collection<UserEvent> getEventByUser(Long id);

    void createEvent(UserEvent userEvent);
}
