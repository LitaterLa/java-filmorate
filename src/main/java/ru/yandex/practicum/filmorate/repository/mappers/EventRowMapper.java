package ru.yandex.practicum.filmorate.repository.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.UserEvent;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class EventRowMapper implements RowMapper<UserEvent> {
    public UserEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        UserEvent userEvent = new UserEvent();
        userEvent.setEventId(rs.getLong("EVENT_ID"));
        userEvent.setUserId(rs.getLong("USER_ID"));
        userEvent.setEntityId(rs.getLong("ENTITY_ID"));
        userEvent.setEventType(UserEvent.EventType.valueOf(rs.getString("EVENT_TYPE_NAME")));
        userEvent.setOperation(UserEvent.EventOperation.valueOf(rs.getString("EVENT_OPERATION_NAME")));
        userEvent.setTimestamp(rs.getLong("EVENT_TIMESTAMP"));

        return userEvent;
    }
}
