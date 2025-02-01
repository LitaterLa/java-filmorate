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

-- Добавляем тестовый фильм
INSERT INTO films (id, name, description, release_date, duration, rating_id)
VALUES (1, '1', 'ztNHd1DaKjDYJSPfExuYfj8ITHn5upRpNzuusiumN0YHyMajEK',
        '1967-09-03', 145, 5),
        (2, '2', 'ztNHd1DaKjDYJSPfExuYfj8ITHn5upRpNzuusiumN0YHyMajEK','1967-09-03', 145, 5),
        (3, '3', 'ztNHd1DaKjDYJSPfExuYfj8ITHn5upRpNzuusiumN0YHyMajEK',
                '1967-09-03', 145, 5);

-- Добавляем жанры для фильма (связь многие ко многим)
INSERT INTO film_genres (film_id, genre_id) VALUES (2, 3);
INSERT INTO film_genres (film_id, genre_id) VALUES (2, 5);

INSERT INTO directors (name) VALUES ('Director A');
INSERT INTO directors (name) VALUES ('Director B');

INSERT INTO film_directors (director_id, film_id) VALUES (1, 1); -- Director A directed Film 1
INSERT INTO film_directors (director_id, film_id) VALUES (2, 2);
INSERT INTO film_directors (director_id, film_id) VALUES (2, 3);
