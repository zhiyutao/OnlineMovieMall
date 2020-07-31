CREATE DATABASE IF NOT EXISTS moviedb; -- DEFAULT CHARACTER SET "utf8";
USE moviedb;

CREATE TABLE movies (
    id VARCHAR(10) NOT NULL,
    title VARCHAR(100) NOT NULL,
    year INT NOT NULL,
    director VARCHAR(100) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE stars (
	id VARCHAR(10) NOT NULL,
    name VARCHAR(100) NOT NULL,
    birthYear INT,
    PRIMARY KEY(id)
);

CREATE TABLE stars_in_movies (
	starId VARCHAR(10) NOT NULL,
	movieId VARCHAR(10) NOT NULL,
	PRIMARY KEY(starId, movieId),
	FOREIGN KEY(starId) REFERENCES stars(id) ON UPDATE CASCADE ON DELETE CASCADE,
	FOREIGN KEY(movieId) REFERENCES movies(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE genres (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(32) NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE genres_in_movies (
    genreId INT NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    PRIMARY KEY(genreId, movieId),
    FOREIGN KEY(genreId) REFERENCES genres(id) ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY(movieId) REFERENCES movies(id) ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE creditcards (
    id VARCHAR(20) NOT NULL,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    expiration DATE NOT NULL,
    PRIMARY KEY(id)
);

CREATE TABLE customers (
    id INT NOT NULL AUTO_INCREMENT,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    ccId varchar(20) NOT NULL,
    address VARCHAR(200) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(ccId) REFERENCES creditcards(id) ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE sales (
    id INT NOT NULL AUTO_INCREMENT,
    customerId INT NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    saleDate DATE NOT NULL,
    PRIMARY KEY(id),
    FOREIGN KEY(customerId) REFERENCES customers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    FOREIGN KEY(movieId) REFERENCES movies(id) ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE ratings (
    movieId VARCHAR(10) NOT NULL,
    rating FLOAT NOT NULL,
    numVotes INT NOT NULL,
    FOREIGN KEY(movieId) REFERENCES movies(id) ON UPDATE CASCADE ON DELETE CASCADE
);


CREATE TABLE employees (
    email varchar(50) primary key,
    password varchar(20) not null,
    fullname varchar(100)
);

INSERT INTO employees VALUES('classta@email.edu', 'classta', 'TA CS122B');
-- shopping cart table
-- CREATE TABLE shopping_cart (
--     customerId INT NOT NULL,
--     movieId VARCHAR(10) NOT NULL,
--     count INT NOT NULL DEFAULT 1,
--     PRIMARY KEY(customerId),
--     FOREIGN KEY(customerId) REFERENCES customers(id) ON UPDATE CASCADE ON DELETE RESTRICT,
--     FOREIGN KEY(movieId) REFERENCES movies(id) ON UPDATE CASCADE ON DELETE RESTRICT
-- );

ALTER TABLE customers ADD UNIQUE index email_unq_index(email);
CREATE INDEX movie_id_index ON ratings (movieId);
CREATE INDEX rating_index ON ratings (rating);
CREATE INDEX star_id_index ON stars_in_movies (starId);
CREATE INDEX movie_id_index ON stars_in_movies (movieId);
CREATE INDEX movie_initial_index ON movies (title(1));
CREATE INDEX movie_title_index ON movies (title);
CREATE INDEX movie_in_year ON movies(year);
CREATE INDEX movie_year_director_title on movies (year, director, title);
CREATE FULLTEXT INDEX movie_title_text_index on movies(title);
CREATE UNIQUE INDEX genres_name on genres (name);
-- CREATE INDEX customer_id_index ON sales (customerId);
-- CREATE INDEX movie_id_index ON sales (movieId);


