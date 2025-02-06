package ru.yandex.practicum.filmorate.model;

import lombok.*;

import java.util.Objects;

@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserEvent {
    private long eventId;
    private long userId;
    private long entityId;
    private EventType eventType;
    private EventOperation operation;
    private long timestamp;

    public enum EventOperation {
        REMOVE,
        ADD,
        UPDATE
    }

    public enum EventType {
        LIKE,
        REVIEW,
        FRIEND
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserEvent userEvent = (UserEvent) o;
        return Objects.equals(userId, userEvent.userId) && Objects.equals(entityId, userEvent.eventId)
               && Objects.equals(eventType, userEvent.eventType)
               && Objects.equals(operation, userEvent.operation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, entityId, eventType);
    }
}
