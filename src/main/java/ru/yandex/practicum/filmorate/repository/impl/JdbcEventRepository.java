package ru.yandex.practicum.filmorate.repository.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.UserEvent;
import ru.yandex.practicum.filmorate.repository.EventRepository;
import ru.yandex.practicum.filmorate.repository.mappers.EventRowMapper;

import java.util.Collection;

@Repository
@RequiredArgsConstructor
@Primary
public class JdbcEventRepository implements EventRepository {
    private final JdbcTemplate jdbcTemplate;
    private final EventRowMapper eventRowMapper;

    @Override
    public Collection<UserEvent> getEventByUser(Long id) {
        String q = "SELECT ue.EVENT_ID, ue.USER_ID, ue.ENTITY_ID, et.EVENT_TYPE_NAME, eo.EVENT_OPERATION_NAME, " +
                   "ue.EVENT_TIMESTAMP FROM USER_EVENTS ue JOIN EVENTS_TYPES et ON ue.EVENT_TYPE_ID = et.EVENT_TYPE_ID " +
                   "JOIN EVENTS_OPERATIONS eo ON ue.EVENT_OPERATION_ID = eo.EVENT_OPERATION_ID " +
                   "WHERE ue.USER_ID = ?";
        return jdbcTemplate.query(q, eventRowMapper, id);
    }

    @Override
    public void createEvent(UserEvent userEvent) {
        String q = "INSERT INTO USER_EVENTS (USER_ID, ENTITY_ID, EVENT_TYPE_ID, EVENT_OPERATION_ID, EVENT_TIMESTAMP) " +
                    "VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(q,
                      userEvent.getUserId(),
                            userEvent.getEntityId(),
                            userEvent.getEventType().ordinal() + 1,
                            userEvent.getOperation().ordinal() + 1,
                            userEvent.getTimestamp());
    }
}
