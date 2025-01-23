MERGE INTO MPAA (id, name)
    VALUES (1, 'G'),
           (2, 'PG'),
           (3, 'PG-13'),
           (4, 'R'),
           (5, 'NC-17');
MERGE INTO genres (id, name) VALUES (1, 'Комедия');
MERGE INTO genres (id, name) VALUES (2, 'Драма');
MERGE INTO genres (id, name) VALUES (3, 'Мультфильм');
MERGE INTO genres (id, name) VALUES (4, 'Триллер');
MERGE INTO genres (id, name) VALUES (5, 'Документальный');
MERGE INTO genres (id, name) VALUES (6, 'Боевик');
