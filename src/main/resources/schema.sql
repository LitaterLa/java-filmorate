drop TABLE IF EXISTS users, films, MPAA, genres, film_genres, likes, friends, friendship_status,
reviews, review_reactions, film_directors, directors, USER_EVENTS, EVENTS_TYPES, EVENTS_OPERATIONS CASCADE;

create TABLE IF NOT EXISTS MPAA (
    id INTEGER PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

create TABLE IF NOT EXISTS users (
    id      BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    login   VARCHAR(100) NOT NULL,
    name    VARCHAR(100) NOT NULL,
    email   VARCHAR(200) NOT NULL,
    birthday DATE        NOT NULL
);

create TABLE IF NOT EXISTS friendship_status (
    id      INTEGER PRIMARY KEY,
    name    VARCHAR(50) NOT NULL
);

create TABLE IF NOT EXISTS genres (
    id      INTEGER PRIMARY KEY,
    name    VARCHAR(100) NOT NULL
);

create table if not exists directors (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL
);

create TABLE IF NOT EXISTS films (
    id          BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(200) NOT NULL,
    release_date DATE NOT NULL,
    duration    INTEGER NOT NULL,
    rating_id   INTEGER NOT NULL REFERENCES MPAA(id) ON delete CASCADE
);

create table if not exists film_directors (
    director_id INTEGER    NOT NULL     REFERENCES directors (id) ON delete CASCADE,
    film_id     INTEGER    NOT NULL     REFERENCES films (id) ON delete CASCADE
);

create TABLE IF NOT EXISTS film_genres (
genre_id INTEGER    NOT NULL     REFERENCES genres (id) ON delete CASCADE,
film_id  INTEGER   NOT NULL      REFERENCES films (id) ON delete CASCADE
);


create TABLE IF NOT EXISTS likes (
film_id INTEGER REFERENCES films (id) ON delete CASCADE,
user_id INTEGER REFERENCES users (id) ON delete CASCADE
);

create TABLE IF NOT EXISTS friends (
    user1_id INTEGER NOT NULL,
    user2_id INTEGER NOT NULL,
    status_id INTEGER,
    PRIMARY KEY (user1_id, user2_id),
    FOREIGN KEY (user1_id) REFERENCES users(id)  ON delete CASCADE,
    FOREIGN KEY (user2_id) REFERENCES users(id)  ON delete CASCADE,
    FOREIGN KEY (status_id) REFERENCES friendship_status(id)
);

create TABLE IF NOT EXISTS reviews (
     review_id      INTEGER    NOT NULL    AUTO_INCREMENT PRIMARY KEY,
     user_id        BIGINT     NOT NULL    REFERENCES users (id) ON delete CASCADE,
     film_id        BIGINT     NOT NULL    REFERENCES films (id) ON delete CASCADE,
     is_positive    BOOLEAN,
     content        VARCHAR(1000)    NOT NULL,
     useful         INTEGER    DEFAULT 0
);

create TABLE IF NOT EXISTS review_reactions (
    review_id   BIGINT   NOT NULL  REFERENCES      reviews(review_id)  ON DELETE CASCADE,
    user_id     BIGINT   NOT NULL  REFERENCES      users(id)           ON DELETE CASCADE,
    is_like     BOOLEAN  NOT NULL,
    UNIQUE(review_id, user_id)
);

CREATE TABLE IF NOT EXISTS USER_EVENTS
(
    EVENT_ID BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    USER_ID BIGINT NOT NULL,
    ENTITY_ID BIGINT NOT NULL,
    EVENT_TYPE_ID BIGINT NOT NULL,
    EVENT_OPERATION_ID BIGINT NOT NULL,
    EVENT_TIMESTAMP BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS EVENTS_TYPES
(
    EVENT_TYPE_ID BIGINT NOT NULL AUTO_INCREMENT,
    EVENT_TYPE_NAME VARCHAR(100) NOT NULL,
    CONSTRAINT EVENTS_TYPES_PK PRIMARY KEY (EVENT_TYPE_ID)
);

CREATE TABLE IF NOT EXISTS EVENTS_OPERATIONS
(
    EVENT_OPERATION_ID   BIGINT  NOT NULL AUTO_INCREMENT,
    EVENT_OPERATION_NAME VARCHAR(100) NOT NULL,
    CONSTRAINT EVENT_OPERATIONS_PK PRIMARY KEY (EVENT_OPERATION_ID)
);

