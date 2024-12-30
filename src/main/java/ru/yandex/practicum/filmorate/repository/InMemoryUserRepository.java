package ru.yandex.practicum.filmorate.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
public class InMemoryUserRepository implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final Map<Long, Set<Long>> userFriends = new HashMap<>();
    private long id = 1;

    @Override
    public void save(User user) {
        if (user.getId() == null) {
            user.setId(generateId());
        }
        users.put(user.getId(), user);
        userFriends.putIfAbsent(user.getId(), new HashSet<>());
    }

    @Override
    public Optional<User> get(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    public Collection<User> getAll() {
        return users.values();
    }

    @Override
    public void delete(Long id) {
        users.remove(id);
        userFriends.remove(id);
        userFriends.values().forEach(friends -> friends.remove(id));
    }

    @Override
    public User update(User user) {
        return Optional.ofNullable(users.get(user.getId())).map(oldUser -> {
            oldUser.setLogin(user.getLogin());
            oldUser.setName(user.getName());
            oldUser.setEmail(user.getEmail());
            oldUser.setBirthday(user.getBirthday());
            users.put(oldUser.getId(), oldUser);
            return oldUser;
        }).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }


    @Override
    public void addFriend(User user, User friend) {
        userFriends.computeIfAbsent(user.getId(), id -> new HashSet<>()).add(friend.getId());
        userFriends.computeIfAbsent(friend.getId(), id -> new HashSet<>()).add(user.getId());
    }

    @Override
    public void deleteFriend(User user, User friend) {
        userFriends.get(user.getId()).remove(friend.getId());
        Set<Long> userFriendsSet = userFriends.computeIfAbsent(user.getId(),
                id -> new HashSet<>());
        userFriendsSet.remove(friend.getId());
        Set<Long> friendFriendsSet = userFriends.computeIfAbsent(friend.getId(),
                id -> new HashSet<>());
        friendFriendsSet.remove(user.getId());
    }

    @Override
    public Set<User> getFriends(Long userId) {
        Set<Long> friendIds = userFriends.getOrDefault(userId, Collections.emptySet());
        Set<User> friends = new HashSet<>();
        for (Long friendId : friendIds) {
            User friend = users.get(friendId);
            if (friend != null) {
                friends.add(friend);
            }
        }
        return new HashSet<>(friends);
    }

    @Override
    public Set<User> getMutualFriends(Long id1, Long id2) {
        Set<Long> friends1 = userFriends.getOrDefault(id1, Collections.EMPTY_SET);
        Set<Long> friends2 = userFriends.getOrDefault(id2, Collections.EMPTY_SET);

        Set<Long> mutualId = new HashSet<>(friends1);
        mutualId.retainAll(friends2);

        Set<User> mutualFriends = new HashSet<>();
        for (Long id : mutualId) {
            User user = users.get(id);
            if (user != null) {
                mutualFriends.add(user);
            }
        }
        return mutualFriends;
    }

    private long generateId() {
        return id++;
    }
}
