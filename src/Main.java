import javax.crypto.SecretKeyFactory;
import javax.swing.*;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;

public class Main {
    Account currentUser;
    boolean running = true;
    boolean adminView;

    public static void main(String[] args) {
        Repository db = new Repository();
        Connection dbconn = db.connectToDatabase("moviedb", "postgres", "!Jmjm1859");
//        db.pullMovies(dbconn);
        Main main = new Main();
        Scanner sc = new Scanner(System.in);
        String userInput;

        while (main.running) {
            if (main.currentUser == null) {
                Account acc = null;
                System.out.println("Welcome! Would you like to [Log In], [Create] Account, or [Q]uit?");
                System.out.print("> ");
                userInput = sc.nextLine().toLowerCase().strip();
                switch (userInput) {
                    case "log in":
                    case "login":
                        acc = main.login(sc);
                        break;
                    case "create":
                        acc = main.createAccount(sc);
                        break;
                    case "q":
                        main.quit(sc);
                        break;
                    default:
                        System.out.println("Please enter a valid action.");
                        break;
                }
                if (acc != null) {
                    main.currentUser = acc;
                    main.adminView = main.currentUser.isAdmin;
                }
            } else if (main.adminView) {
                System.out.printf("Welcome %s, what would you like to do today?\n", main.currentUser.username);
                System.out.println("Manage [Movies], Manage [Users], Manage [Directors], View as [Customer], or [Log out]");
                System.out.print("> ");
                userInput = sc.nextLine().toLowerCase().strip();
                switch (userInput) {
                    case "movies":
                        break;
                    case "users":
                        break;
                    case "directors":
                        break;
                    case "customer":
                        main.adminView = false;
                        break;
                    case "logout":
                    case "log out":
                        main.currentUser = null;
                        break;
                    default:
                        System.out.println("Please enter a valid input.");
                        break;
                }
            } else {
                System.out.printf("Welcome %s, what would you like to do today?\n", main.currentUser.username);
                System.out.println("[View] movies");
                System.out.print("> ");
                userInput = sc.nextLine().toLowerCase().strip();
                switch (userInput) {
                    case "view":
                        main.viewMovies(sc);
                }
            }
        }
    }

    public Account login(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        try {
            Repository repo = new Repository();
            Connection conn = repo.connectToDatabase("moviedb", "postgres", "!Jmjm1859");
            Account found_account = repo.getAccount(conn, username, password);
            if (found_account == null) {
                System.out.println("Could not find any account matching that Username/Password combination.");
            }
            return found_account;

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }

    public Account createAccount(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Confirm Password: ");
        String password2 = scanner.nextLine();
        if (password.equals(password2)) {
            Repository repo = new Repository();
            Connection conn = repo.connectToDatabase("moviedb", "postgres", "!Jmjm1859");
            repo.createAccount(conn, username, password, false);
            return repo.getAccount(conn, username, password);
        } else {
            System.out.println("Error: Passwords must match!");
            return null;
        }
    }

    public void quit(Scanner scanner) {
        this.running = false;
        scanner.close();
    }

    public void manageMovies(Scanner scanner) {
        String userInput;
        while (true) {
            System.out.println("[Add] new movies, [Delete] movies, [Update] movies, [View] movies, or [Back]");
            System.out.print("> ");
            userInput = scanner.nextLine().toLowerCase().strip();
            switch (userInput) {
                case "add":
                    break;
                case "delete":
                    break;
                case "update":
                    break;
                case "back":
                    return;
                default:
                    System.out.println("Please enter a valid input.");
            }
        }
    }

    public void viewMovies(Scanner scanner) {
        String userInput;
        while (true) {
            boolean skip = false;
            System.out.println("What would you like to search by?");
            System.out.println("[Genre], [Year], [Runtime], [Title], [Available], [All] (or [Back] to exit)");
            System.out.print("> ");
            userInput = scanner.nextLine().toLowerCase().strip();
            String [] choices = {"genre", "year", "runtime", "title"};
            for (String value : choices) {
                if (userInput.equals(value)) {
                    Repository repo = new Repository();
                    Connection conn = repo.connectToDatabase("moviedb", "postgres", "!Jmjm1859");
                    repo.filterMovies(conn, value);
                    skip = true;
                }
            }

            if (!skip) {
                switch (userInput) {
                    case "back":
                        return;
                    case "all":
                        Repository repo = new Repository();
                        Connection conn = repo.connectToDatabase("moviedb", "postgres", "!Jmjm1859");
                        repo.pullMovies(conn);
                        break;
                    case "available":
                        Repository repo2 = new Repository();
                        Connection conn2 = repo2.connectToDatabase("moviedb", "postgres", "!Jmjm1859");
                        repo2.availableMovies(conn2);
                        break;
                    default:
                        System.out.println("Please enter a valid action!");
                }
            }

        }
    }
}