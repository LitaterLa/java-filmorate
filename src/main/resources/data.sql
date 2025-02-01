MERGE INTO MPAA (id, name)
    VALUES (1, 'G'),
           (2, 'PG'),
           (3, 'PG-13'),
           (4, 'R'),
           (5, 'NC-17');

MERGE INTO genres (id, name)
    VALUES (1, 'Комедия'),
           (2, 'Драма'),
           (3, 'Мультфильм'),
           (4, 'Триллер'),
           (5, 'Документальный'),
           (6, 'Боевик');

INSERT INTO EVENTS_TYPES (EVENT_TYPE_NAME)
    VALUES ('LIKE'),
           ('REVIEW'),
           ('FRIEND');

INSERT INTO EVENTS_OPERATIONS (EVENT_OPERATION_NAME)
    VALUES ('REMOVE'),
           ('ADD'),
           ('UPDATE');

INSERT INTO films (id, name, description, release_date, duration, rating_id)
VALUES (1, 'New film', 'New film about friends', '1999-04-30', 120, 3),
       (2, 'New film with director', 'Film with director', '1999-04-30', 120, 3),
       (3, 'Old classic', 'Classic film', '1980-06-15', 100, 4);

INSERT INTO film_genres (film_id, genre_id) VALUES (1, 1), (1, 2);
INSERT INTO film_genres (film_id, genre_id) VALUES (2, 1);
INSERT INTO film_genres (film_id, genre_id) VALUES (3, 3);

MERGE INTO directors (id, name)
    VALUES (1, 'Director updated');

INSERT INTO film_directors (director_id, film_id) VALUES (1, 2);

INSERT INTO users (id, login, name, email, birthday)
VALUES (1, 'user1', 'User One', 'user1@example.com', '1990-01-01'),
       (2, 'user2', 'User Two', 'user2@example.com', '1992-02-02');

INSERT INTO likes (film_id, user_id)
VALUES (1, 1), (1, 2), (2, 1);

ALTER TABLE films ALTER COLUMN id RESTART WITH 4;