import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {
    public static Connection connectToDatabase() {
        try{
            return DriverManager.getConnection("jdbc:postgresql://localhost:5432/moviedb", "postgres", "!Jmjm1859");
        } catch (Exception e) {
            throw new RuntimeException("Error Connecting to Database");
        }
    }
}
