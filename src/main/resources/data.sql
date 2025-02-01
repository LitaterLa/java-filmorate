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
VALUES
    (1, 'Film 1', 'Description 1', '1967-09-03', 145, 5),
    (2, 'Film 2', 'Description 2', '1967-09-03', 145, 4),
    (3, 'Film 3', 'Description 3', '1967-09-03', 145, 3);

INSERT INTO film_genres (film_id, genre_id)
VALUES
    (1, 1),
    (2, 2),
    (2, 3),
    (3, 4);

INSERT INTO directors (id, name)
VALUES
    (1, 'Director A'),
    (2, 'Director B');

INSERT INTO film_directors (film_id, director_id)
VALUES
    (1, 1),
    (2, 1),
    (3, 2);

INSERT INTO users (id, login, name, email, birthday)
VALUES
    (1, 'user1', 'User One', 'user1@example.com', '1980-01-01'),
    (2, 'user2', 'User Two', 'user2@example.com', '1990-02-02');

INSERT INTO likes (film_id, user_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 1);

ALTER TABLE films ALTER COLUMN id RESTART WITH 4;
ALTER TABLE users ALTER COLUMN id RESTART WITH 3;