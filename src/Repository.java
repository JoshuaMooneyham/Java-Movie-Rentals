//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
import java.sql.*;
import java.util.ArrayList;

public class Repository {
    public Connection connectToDatabase(String dbname, String user, String pass) {
        Connection conn = null;
        try{
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/"+dbname, user, pass);
            if (conn != null) {
                System.out.println("Connection Established");
            } else {
                System.out.println("Connection Failed");
            }
        } catch (Exception e) {
            System.out.println("Error Connecting to Database");
        }
        return conn;
    }

    public ArrayList<Movie> pullMovies(Connection conn) {
        PreparedStatement statement;
        try {
            String sql = """
                    SELECT movies.id, name, title, year, runtime, genre, rented_by
                    FROM movies
                    INNER JOIN director ON movies.director_id = director.id
                    ORDER BY title;""";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            ArrayList<Movie> movieList = new ArrayList<>();
            while (test.next()) {
                String [] genre = {};
                try {
                    genre = test.getArray("genre").toString().split(",");
                    for (int i = 0; i < genre.length; i++) {
                        genre[i] = genre[i].replaceAll("[{}]", "");
                    }
                } catch (NullPointerException e) {
                }
                movieList.add(new Movie(test.getInt("id"), this.getDirector(conn, test.getString("name")), test.getString("title"), test.getInt("year"), test.getInt("runtime"), genre, test.getInt("rented_by")));
            }
            return movieList;
        } catch (Exception e) {
            System.out.println("Error pulling all movies");
            return null;
        }
    }

    public ArrayList<Movie> rentedMovies(Connection conn, Account user) {
        PreparedStatement statement;
        try {
            String sql = """
                    SELECT movies.id, name, title, year, runtime, genre, rented_by
                    FROM movies
                    INNER JOIN director ON movies.director_id = director.id
                    WHERE rented_by = ?
                    ORDER BY title;""";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, user.id);
            ResultSet test = statement.executeQuery();
            ArrayList<Movie> movieList = new ArrayList<>();
            while (test.next()) {
                String [] genre = {};
                try {
                    genre = test.getArray("genre").toString().split(",");
                    for (int i = 0; i < genre.length; i++) {
                        genre[i] = genre[i].replaceAll("[{}]", "");
                    }
                } catch (NullPointerException e) {
                }
                movieList.add(new Movie(test.getInt("id"), this.getDirector(conn, test.getString("name")), test.getString("title"), test.getInt("year"), test.getInt("runtime"), genre, test.getInt("rented_by")));
            }
            return movieList;
        } catch (Exception e) {
            System.out.println("Error pulling rented movies");
            return null;
        }
    }

    public ArrayList<Movie> availableMovies(Connection conn) {
        PreparedStatement statement;
        try {
            String sql = """
                    SELECT movies.id, name, title, year, runtime, genre, rented_by
                    FROM movies
                    INNER JOIN director ON movies.director_id = director.id
                    WHERE rented_by IS NULL
                    ORDER BY title;""";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            ArrayList<Movie> movieList = new ArrayList<>();
            while (test.next()) {
                String [] genre = {};
                try {
                    genre = test.getArray("genre").toString().split(",");
                    for (int i = 0; i < genre.length; i++) {
                        genre[i] = genre[i].replaceAll("[{}]", "");
                    }
                } catch (NullPointerException e) {
                }
                movieList.add(new Movie(test.getInt("id"), this.getDirector(conn, test.getString("name")), test.getString("title"), test.getInt("year"), test.getInt("runtime"), genre, test.getInt("rented_by")));
            }
            return movieList;
        } catch (Exception e) {
            System.out.println("Error pulling available movies");
            return null;
        }
    }

    public void filterMovies(Connection conn, String filter) {
        PreparedStatement statement;
        try {
            String sql = "SELECT * FROM movies ORDER BY " + filter + ";";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            while (test.next()) {
                System.out.println(String.format("%s %s %s %s %s %s %s", test.getInt("id"), test.getInt("director_id"), test.getString("title"), test.getInt("year"), test.getInt("runtime"), test.getArray("genre"), test.getInt("rented_by")));
            }
        } catch (Exception e) {
            System.out.println("Error filtering movies");
        }
    }

    public void printAvailableMovies(Connection conn) {
        PreparedStatement statement;
        try {
            String sql = "SELECT * FROM movies WHERE rented_by IS NULL;";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            while (test.next()) {
                System.out.println(String.format("%s %s %s %s %s %s %s", test.getInt("id"), test.getInt("director_id"), test.getString("title"), test.getInt("year"), test.getInt("runtime"), test.getArray("genre"), test.getInt("rented_by") != 0 ? "Rented" : "Available"));
            }
        } catch (Exception e) {
            System.out.println("Error printing available movies");
        }
    }

    public void createMovie(Connection conn, Director director, String title, Integer year, Integer runtime, String[] genre) {
        PreparedStatement statement;
        try {

            Movie duplicate = this.findDuplicate(conn, title);
            if (duplicate != null) {
                this.updateMovie(conn, duplicate, duplicate.director, String.format("%s (%s)", duplicate.title, duplicate.year), duplicate.year, duplicate.runtime, duplicate.genre);
                title = String.format("%s (%s)", title, year != null ? year : "Unknown Year");
            }

            String sql = """
                    INSERT INTO movies (director_id, title, year, runtime, genre)
                        VALUES (?, ?, ?, ?, ?)""";
            statement = conn.prepareStatement(sql);

            statement.setInt(1, director.id);

            statement.setString(2, title);

            if (year != null) {
                statement.setInt(3, year);
            } else {
                statement.setNull(3, Types.INTEGER);
            }

            if (runtime != null) {
                statement.setInt(4, runtime);
            } else {
                statement.setNull(4, Types.INTEGER);
            }

            if (genre.length != 0) {
                statement.setArray(5, conn.createArrayOf("VARCHAR", genre));
            } else {
                statement.setNull(5, Types.ARRAY);
            }

            statement.execute();
            statement.close();

            System.out.println(String.format("Created Movie %s", title));

        } catch (Exception e) {
            System.out.println("Error creating movie");
        }
    }

    public Movie getMovie(Connection conn, int id) {
        PreparedStatement statement;
        try {
            String sql = """
                    SELECT movies.id, name, title, year, runtime, genre, rented_by
                    FROM movies
                    INNER JOIN director ON movies.director_id = director.id
                    WHERE movies.id = ?;""";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet test = statement.executeQuery();
            test.next();
            String [] genre = {};
            try {
                genre = test.getArray("genre").toString().split(",");
                for (int i = 0; i < genre.length; i++) {
                    genre[i] = genre[i].replaceAll("[{}]", "");
                }
            } catch (NullPointerException e) {
            }
            return new Movie(test.getInt("id"), this.getDirector(conn, test.getString("name")), test.getString("title"), test.getInt("year"), test.getInt("runtime"), genre, test.getInt("rented_by"));
        } catch (Exception e) {
            System.out.println("Error getting movie");
            return null;
        }
    }

    public Movie findDuplicate(Connection conn, String title) {
        PreparedStatement statement;
        try {
            String sql = """
                    SELECT movies.id, name, title, year, runtime, genre, rented_by
                    FROM movies
                    INNER JOIN director ON movies.director_id = director.id
                    WHERE title = ?;""";
            statement = conn.prepareStatement(sql);
            statement.setString(1, title);
            ResultSet test = statement.executeQuery();
            test.next();
            String [] genre = {};
            try {
                genre = test.getArray("genre").toString().split(",");
                for (int i = 0; i < genre.length; i++) {
                    genre[i] = genre[i].replaceAll("[{}]", "");
                }
            } catch (NullPointerException e) {
            }
            return new Movie(test.getInt("id"), this.getDirector(conn, test.getString("name")), test.getString("title"), test.getInt("year"), test.getInt("runtime"), genre, test.getInt("rented_by"));

        } catch (Exception e) {
            System.out.println("Error finding Duplicate");
            return null;
        }
    }

    public void updateMovie(Connection conn, Movie movie, Director director, String title, Integer year, Integer runtime, String[] genre) {
        PreparedStatement statement;
        try {
            String sql = """
                    UPDATE movies
                    SET director_id = ?,
                        title = ?,
                        year = ?,
                        runtime = ?,
                        genre = ?
                    WHERE id = ?;""";
            statement = conn.prepareStatement(sql);
//            System.out.printf("%s %s %s %s %s %s\n", movie.id, director.id, title, year, runtime, genre.toString());
            if (director != null) {
                statement.setInt(1, director.id);
            } else {
                statement.setNull(1, Types.INTEGER);
            }

            if (title.contains(String.format("(%s)", movie.year)) || title.contains("Unknown Year")) {
                title = title.replace("(Unknown Year)", String.format("(%s)", year));
                title = title.replace(String.format("(%s)", movie.year), String.format("(%s)", year));
            }
            statement.setString(2, title);

            if (year != null) {
                statement.setInt(3, year);
            } else {
                statement.setNull(3, Types.INTEGER);
            }

            if (runtime != null) {
                statement.setInt(4, runtime);
            } else {
                statement.setNull(4, Types.INTEGER);
            }

            if (genre.length != 0) {
                statement.setArray(5, conn.createArrayOf("VARCHAR", genre));
            } else {
                statement.setNull(5, Types.ARRAY);
            }

            statement.setInt(6, movie.id);

            statement.execute();
            statement.close();

            System.out.printf("Movie %s '%s' Updated", movie.id, title);
        } catch (Exception e) {
            System.out.println("Error updating movie");
        }
    }

    public void deleteMovie(Connection conn, Movie movie) {
        PreparedStatement statement;
        try {
            String sql = "DELETE FROM movies WHERE id = ?;";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, movie.id);
            statement.execute();
            statement.close();
        } catch (Exception e) {
            System.out.println("Error deleting movie");
        }
    }

    public void rentMovie(Connection conn, Account account, Movie movie) {
        PreparedStatement statement;
        try {
            String sql = """
                    UPDATE movies
                    SET rented_by = ?
                    WHERE id = ?;""";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, account.id);
            statement.setInt(2, movie.id);
            ResultSet test = statement.executeQuery();
            System.out.println("Rented " + movie.title);
        } catch (Exception e) {
            System.out.println("Error renting movie");
        }
    }

    public void returnMovie(Connection conn, Movie movie) {
        PreparedStatement statement;
        try {
            String sql = """
                    UPDATE movies
                    SET rented_by = ?
                    WHERE id = ?;""";
            statement = conn.prepareStatement(sql);
            statement.setNull(1, Types.INTEGER);
            statement.setInt(2, movie.id);
            ResultSet test = statement.executeQuery();
            System.out.println("Returned " + movie.title);
        } catch (Exception e) {
            System.out.println("Error returning movie");
        }
    }

    public Account createAccount(Connection conn, String username, String password, boolean isAdmin) {
        PreparedStatement statement;
        try {
            String sql = """
                    INSERT INTO users (username, password, admin)
                    VALUES (?, ?, ?)""";
            statement = conn.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setBoolean(3, isAdmin);
            statement.execute();
            statement.close();
            System.out.println("Account Created");
            return this.getAccount(conn, username, password);
        } catch (Exception e) {
            System.out.println("Error creating account");
            return null;
        }
    }

    public Account getAccount(Connection conn, String username, String password) {
        PreparedStatement statement;
        try {
            String sql = """
                    SELECT * FROM users WHERE username = ? AND password = ?;""";
            statement = conn.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            ResultSet results = statement.executeQuery();
            Account user = null;
            while (results.next()) {
                user = new Account(results.getInt("id"), results.getString("username"), results.getBoolean("admin"));
            }
            return user;
        } catch (Exception e) {
            System.out.println("Error getting account");
            return null;
        }
    }

    public ArrayList<Account> pullCustomers(Connection conn) {
        PreparedStatement statement;
        try {
            String sql = """
                    SELECT username, id, admin
                    FROM users
                    WHERE NOT admin
                    ORDER BY id;""";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            ArrayList<Account> customerList = new ArrayList<>();
            while (test.next()) {
                customerList.add(new Account(test.getInt("id"), test.getString("username"), test.getBoolean("admin")));
            }
            return customerList;
        } catch (Exception e) {
            System.out.println("Error pulling customers");
            return null;
        }
    }

    public ArrayList<Account> pullAdmin(Connection conn) {
        PreparedStatement statement;
        try {
            String sql = """
                    SELECT username, id, admin
                    FROM users
                    WHERE admin
                    ORDER BY id;""";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            ArrayList<Account> customerList = new ArrayList<>();
            while (test.next()) {
                customerList.add(new Account(test.getInt("id"), test.getString("username"), test.getBoolean("admin")));
            }
            return customerList;
        } catch (Exception e) {
            System.out.println("Error pulling admin");
            return null;
        }
    }

    public void promoteCustomer(Connection conn, Account user) {
        PreparedStatement statement;
        try {
            String sql = """
                    UPDATE users
                    SET admin = ?
                    WHERE id = ?""";
            statement = conn.prepareStatement(sql);
            statement.setBoolean(1, true);
            statement.setInt(2, user.id);
            statement.execute();
            statement.close();
        } catch (Exception e) {
            System.out.println("Error promoting customers");
        }
    }

    public void demoteAdmin(Connection conn, Account user) {
        PreparedStatement statement;
        try {
            String sql = """
                    UPDATE users
                    SET admin = ?
                    WHERE id = ?""";
            statement = conn.prepareStatement(sql);
            statement.setBoolean(1, false);
            statement.setInt(2, user.id);
            statement.execute();
            statement.close();
        } catch (Exception e) {
            System.out.println("Error demoting user");
        }
    }

    public ArrayList<Account> pullUsers(Connection conn) {
        PreparedStatement statement;
        try {
            String sql = """
                    SELECT username, id, admin
                    FROM users
                    ORDER BY NOT admin;""";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            ArrayList<Account> customerList = new ArrayList<>();
            while (test.next()) {
                customerList.add(new Account(test.getInt("id"), test.getString("username"), test.getBoolean("admin")));
            }
            return customerList;
        } catch (Exception e) {
            System.out.println("Error pulling admin");
            return null;
        }
    }

    public void deleteUser(Connection conn, Account account) {
        PreparedStatement statement;
        try {
            String sql = "DELETE FROM users WHERE id = ?;";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, account.id);
            statement.execute();
            statement.close();
        } catch (Exception e) {
            System.out.println("Error deleting movie");
        }
    }

    public ArrayList<Director> pullDirectors(Connection conn) {
        PreparedStatement statement;
        try {
            String sql = "SELECT * FROM director;";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            ArrayList<Director> directorList = new ArrayList<>();
            while (test.next()) {
                directorList.add(new Director(test.getInt("id"), test.getString("name")));
            }
            return directorList;
        } catch (Exception e) {
            System.out.println("Error pulling all directors");
            return null;
        }
    }

    public Director createDirector(Connection conn, String name) {
        PreparedStatement statement;
        try {
            String sql = """
                    INSERT INTO director (name)
                        VALUES (?);
                    """;
            statement = conn.prepareStatement(sql);
            statement.setString(1, name);
            statement.execute();
            statement.close();
            System.out.println("Created New Director Entry: " + name);
            return this.getDirector(conn, name);
        } catch (Exception e) {
            System.out.println("Error creating director");
            return null;
        }
    }

    public Director getDirector(Connection conn, String name) {
        PreparedStatement statement;
        try {
            String sql = "SELECT * FROM director WHERE name=?;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, name);
            ResultSet test = statement.executeQuery();
            test.next();
            return new Director(test.getInt("id"), test.getString("name"));
        } catch (Exception e) {
            System.out.println("Error getting director");
            return null;
        }
    }

    public void updateDirector(Connection conn, Director director) {
        PreparedStatement statement;
        try {
            String sql = "UPDATE director SET name = ? WHERE id = ?;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, director.name);
            statement.setInt(2, director.id);
            statement.execute();
            statement.close();
        } catch (Exception e) {
            System.out.println("Error getting director");
        }
    }

    public void deleteDirector(Connection conn, Director director) {
        PreparedStatement statement;
        try {
            String sql = "DELETE FROM director WHERE id = ?;";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, director.id);
            statement.execute();
            statement.close();
        } catch (Exception e) {
            System.out.println("Error getting director");
        }
    }
}
