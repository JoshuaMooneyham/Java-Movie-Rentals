//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

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
            System.out.println(e);
        }
        return conn;
    }

//    public void createTable(Connection conn, String table_name) {
//        Statement statement;
//        try {
//            String query = "create table " + table_name + "(empid SERIAL, name varchar(200), address varchar(200), primary key(empid));";
//            statement = conn.createStatement();
//            statement.executeUpdate(query);
//            System.out.println("Table Created");
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }
//
//    public void insert_row(Connection conn, String table_name, String name, String address) {
//        Statement statement;
//        try {
//            String query = String.format("insert into %s(name, address) values('%s', '%s');", table_name, name, address);
//            statement = conn.createStatement();
//            statement.executeUpdate(query);
//            System.out.println("Row Inserted");
//        } catch (Exception e) {
//            System.out.println(e);
//        }
//    }

    public void pullMovies(Connection conn) {
        PreparedStatement statement;
        try {
            String sql = "SELECT * FROM movies ORDER BY id;";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            while (test.next()) {
                System.out.println(String.format("%s %s %s %s %s %s %s", test.getInt("id"), test.getInt("director_id"), test.getString("title"), test.getInt("year"), test.getInt("runtime"), test.getArray("genre"), test.getInt("rented_by")));
            }
        } catch (Exception e) {
            System.out.println(e);
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
            System.out.println(e);
        }
    }

    public void availableMovies(Connection conn) {
        PreparedStatement statement;
        try {
            String sql = "SELECT * FROM movies WHERE rented_by IS NULL;";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            while (test.next()) {
                System.out.println(String.format("%s %s %s %s %s %s %s", test.getInt("id"), test.getInt("director_id"), test.getString("title"), test.getInt("year"), test.getInt("runtime"), test.getArray("genre"), test.getInt("rented_by") != 0 ? "Rented" : "Available"));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void pullDirectors(Connection conn) {
        PreparedStatement statement;
        try {
            String sql = "SELECT * FROM director;";
            statement = conn.prepareStatement(sql);
            ResultSet test = statement.executeQuery();
            while (test.next()) {
                System.out.println(String.format("%s %s %s", test.getInt("id"), test.getString("name"), test.getInt("age")));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void createDirector(Connection conn, String name, int age) {
        PreparedStatement statement;
        try {
            String sql = """
                    INSERT INTO director (name, age)
                        VALUES (?, ?);
                    """;
            statement = conn.prepareStatement(sql);
            statement.setString(1, name);
            statement.setInt(2, age);
            ResultSet test = statement.executeQuery();
            System.out.println("Created New Director Entry: " + name);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public Director getDirector(Connection conn, String name) {
        PreparedStatement statement;
        try {
            String sql = "SELECT * FROM director WHERE name=?;";
            statement = conn.prepareStatement(sql);
            statement.setString(1, name);
            ResultSet test = statement.executeQuery();
            System.out.println(test);
            test.next();
//            return test.getInt("id");
            return new Director(test.getInt("id"), test.getString("name"), test.getInt("age"));
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public void createMovie(Connection conn, String director, String title, int year, int runtime, String[] genre) {
        PreparedStatement statement;
        try {
            Director foundDirector = this.getDirector(conn, director);
            String sql = """
                    INSERT INTO movies (director_id, title, year, runtime, genre)
                        VALUES (?, ?, ?, ?, ?)""";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, foundDirector.id);
            statement.setString(2, title);
            statement.setInt(3, year);
            statement.setInt(4, runtime);
            statement.setArray(5, conn.createArrayOf("VARCHAR", genre));
            ResultSet test = statement.executeQuery();
            System.out.println(test);

        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void updateMovie(Connection conn, Movie movie, Director director, String title, int year, int runtime, String[] genre) {
        PreparedStatement statement;
        try {
            String sql = """
                    UPDATE movies
                    SET director_id = ?,
                        title = ?,
                        year = ?,
                        runtime = ?,
                        genre = ?,
                    WHERE id = ?;""";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, director.id);
            statement.setString(2, title);
            statement.setInt(3, year);
            statement.setInt(4, runtime);
            statement.setArray(5, conn.createArrayOf("VARCHAR", genre));
            statement.setInt(6, movie.id);
            ResultSet results = statement.executeQuery();
            System.out.printf("Movie %s '%s' Updated", movie.id, title);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

//    public Movie getMovie(Connection conn, String title) {
//        PreparedStatement statement;
//        try {
//            String sql = "SELECT * FROM movie WHERE title=?;";
//            statement = conn.prepareStatement(sql);
//            statement.setString(1, title);
//            ResultSet test = statement.executeQuery();
//            System.out.println(test);
//            test.next();
////            return test.getInt("id");
//            return new Movie(test.getInt("id"), this.getDirector(conn, test.getInt("director_id")).getString("name"), test.getInt("age"));
//        } catch (Exception e) {
//            System.out.println(e);
//            return null;
//        }
//    }

    public void rentMovie(Connection conn, Account account, int movieId) {
        PreparedStatement statement;
        try {
            String sql = """
                    UPDATE movies
                    SET rented_by = ?
                    WHERE id = ?;""";
            statement = conn.prepareStatement(sql);
            statement.setInt(1, account.id);
            statement.setInt(2, movieId);
            ResultSet test = statement.executeQuery();
            System.out.println(test);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void returnMovie(Connection conn, int movieId) {
        PreparedStatement statement;
        try {
            String sql = """
                    UPDATE movies
                    SET rented_by = ?
                    WHERE id = ?;""";
            statement = conn.prepareStatement(sql);
            statement.setNull(1, Types.INTEGER);
            statement.setInt(2, movieId);
            ResultSet test = statement.executeQuery();
            System.out.println(test);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void createAccount(Connection conn, String username, String password, boolean isAdmin) {
        PreparedStatement statement;
        try {
            String sql = """
                    INSERT INTO users (username, password, admin)
                    VALUES (?, ?, ?)""";
            statement = conn.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);
            statement.setBoolean(3, isAdmin);
            ResultSet newAcc = statement.executeQuery();
            System.out.println("Account Created");
        } catch (Exception e) {
            System.out.println(e);
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
                user = new Account(results.getInt("id"), results.getString("username"), results.getString("password"), results.getBoolean("admin"));
            }
            return user;
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
