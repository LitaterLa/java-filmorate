package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.UserEvent;

import java.util.Collection;


public interface EventService {
    Collection<UserEvent> getEventByUser(long userId);

    void createEvent(long userId, long entityId, UserEvent.EventType eventType, UserEvent.EventOperation eventOperation);
}
