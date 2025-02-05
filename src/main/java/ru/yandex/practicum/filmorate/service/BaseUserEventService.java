package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.repository.EventRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
@Primary
public class BaseUserEventService implements EventService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public Collection<UserEvent> getEventByUser(long userId) {
        if (userRepository.get(userId).isEmpty()) {
            throw new NotFoundException("Пользователь не найден!");
        }

        return eventRepository.getEventByUser(userId);
    }

    @Override
    public void createEvent(long userId, long entityId, UserEvent.EventType eventType, UserEvent.EventOperation eventOperation) {
        UserEvent userEvent = new UserEvent();

        userEvent.setUserId(userId);
        userEvent.setEntityId(entityId);
        userEvent.setEventType(eventType);
        userEvent.setOperation(eventOperation);
        userEvent.setTimestamp(Instant.now().toEpochMilli());

        eventRepository.createEvent(userEvent);
    }
}
