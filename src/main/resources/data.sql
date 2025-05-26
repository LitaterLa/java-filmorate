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
MERGE INTO EVENTS_TYPES (EVENT_TYPE_ID, EVENT_TYPE_NAME)
    VALUES (1, 'LIKE'),
           (2, 'REVIEW'),
           (3, 'FRIEND');

INSERT INTO EVENTS_OPERATIONS (EVENT_OPERATION_ID, EVENT_OPERATION_NAME)
    VALUES (1, 'REMOVE'),
           (2, 'ADD'),
           (3, 'UPDATE');
           