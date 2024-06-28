import java.util.ArrayList;

public class Account {
    public int id;
    public String username;
    public boolean isAdmin;

    public Account(int id, String username, boolean admin) {
        this.id = id;
        this.username = username;
        this.isAdmin = admin;
    }

    public String toString() {
        return String.format("User %s: %s", this.id, this.username);
    }
}
