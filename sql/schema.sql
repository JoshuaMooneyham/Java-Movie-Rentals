CREATE TABLE director (
	id INT GENERATED ALWAYS AS IDENTITY,
	name VARCHAR(255) NOT NULL,
	PRIMARY KEY(id)
);


CREATE TABLE users (
    id INT GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    admin BOOLEAN,
    PRIMARY KEY (id),
    UNIQUE (username)
);


CREATE TABLE movies (
	id INT GENERATED ALWAYS AS IDENTITY,
	director_id INT NOT NULL,
	title VARCHAR(255),
	year INT,
	runtime INT,
	genre VARCHAR(255) [],
	rented_by INT,
	PRIMARY KEY(id),
	CONSTRAINT fk_director
		FOREIGN KEY(director_id)
			REFERENCES director(id)
			ON DELETE CASCADE,

	CONSTRAINT fk_users
	    FOREIGN KEY(rented_by)
	        REFERENCES users(id)
	        ON DELETE SET NULL
);