MERGE INTO MPAA (id, name)
    VALUES (1, 'G'),
           (2, 'PG'),
           (3, 'PG-13'),
           (4, 'R'),
           (5, 'NC-17');
MERGE INTO genres (id, name) VALUES (1, 'comedy');
MERGE INTO genres (id, name) VALUES (2, 'action');
MERGE INTO genres (id, name) VALUES (3, 'adventrure');
MERGE INTO genres (id, name) VALUES (4, 'detective');
MERGE INTO genres (id, name) VALUES (5, 'thriller');
MERGE INTO genres (id, name) VALUES (6, 'sci-fi');
MERGE INTO genres (id, name) VALUES (7, 'horror');
MERGE INTO genres (id, name) VALUES (8, 'documentary');
