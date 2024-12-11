package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
public class InMemoryStorage<T> {
    private final Map<Long, T> storage = new HashMap<>();
    private long id = 10;

    public void save(Long id, T value) {
        storage.put(id, value);
    }

    public T get(long id) {
        return storage.get(id);
    }

    public Collection< T> getAll() {
        return storage.values();
    }

    public void delete(long id) {
        storage.remove(id);
    }

    public long generateId() {
        return ++id;
    }

}
