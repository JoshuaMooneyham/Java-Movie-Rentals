CREATE TABLE director (
	id INT GENERATED ALWAYS AS IDENTITY,
	name VARCHAR(255) NOT NULL,
	age INT,
	PRIMARY KEY(id)
);

CREATE TABLE movies (
	id INT GENERATED ALWAYS AS IDENTITY,
	director_id INT,
	title VARCHAR(255),
	year INT,
	runtime INT,
	genre VARCHAR(255) [],
	rented_by INT,
	PRIMARY KEY(id),
	CONSTRAINT fk_director
		FOREIGN KEY(director_id)
			REFERENCES director(id)
			ON DELETE SET NULL

	CONSTRAINT fk_user
	    FOREIGN KEY(rented_by)
	        REFERENCES user(id)
	        ON DELETE SET NULL
);

CREATE TABLE user (
    id INT GENERATED ALWAYS AS IDENTITY,
    username VARCHAR(255),
    password VARCHAR(255),
    admin BOOLEAN;
    PRIMARY KEY(id)
);