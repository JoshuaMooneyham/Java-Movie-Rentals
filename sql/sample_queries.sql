SELECT movies.id, name, title, year, runtime, genre, rented_by
FROM movies
INNER JOIN director ON movies.director_id = director.id
ORDER BY title;


SELECT movies.id, username, title, year, runtime, genre, rented_by
FROM movies
INNER JOIN users ON movies.rented_by = users.id
ORDER BY title;


SELECT name FROM director
ORDER BY id;


SELECT id, username, password, admin FROM users
WHERE username = ? AND password = ?;


SELECT movies.id, name, title, year, runtime, genre, rented_by
FROM movies
INNER JOIN director ON movies.director_id = director.id
WHERE rented_by IS NULL
ORDER BY movies.id;


