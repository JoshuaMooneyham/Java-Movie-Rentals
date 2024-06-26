import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    Account currentUser;
    boolean running = true;
    boolean adminView;

    public static void main(String[] args) {
        Repository db = new Repository();
        Connection dbconn = db.connectToDatabase("moviedb", "postgres", "!Jmjm1859");
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
                        main.manageMovies(sc);
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
                    this.addMovie(scanner);
                    break;
                case "view":
                    this.viewMovies(scanner);
                    break;
                case "delete":
                    this.deleteMovies(scanner);
                    break;
                case "update":
                    this.updateMovie(scanner);
                    break;
                case "back":
                    return;
                default:
                    System.out.println("Please enter a valid input.");
            }
        }
    }

    public void addMovie(Scanner scanner) {
        String title = "";
        System.out.print("Title (or [Back] to cancel): ");
        while (title.length() < 2) {
            title = this.toTitleCase(scanner.nextLine().strip());
            if (title.equals("Back")) {
                return;
            } else if (title.length() < 2) {
                System.out.print("Please enter a valid title or [Back]: ");
            }
        }
        String director = "";
        System.out.print("Directed by (Name or [Back] to cancel): ");
        while (director.length() < 2) {
            director = this.toTitleCase(scanner.nextLine().strip());
            if (director.equals("Back")) {
                return;
            } else if (director.length() < 2) {
                System.out.print("Please enter a valid Director Name or [Back]: ");
            }
        }
        System.out.print("Year (YYYY or leave blank): ");
        String year = scanner.nextLine().strip();
        System.out.print("Runtime (in minutes or leave blank): ");
        String runtime = scanner.nextLine().strip();
        System.out.print("Genre (separated by ',' or leave blank): ");
        String genre = scanner.nextLine().strip();

        Repository db = new Repository();
        Connection dbconn = db.connectToDatabase("moviedb", "postgres", "!Jmjm1859");

        Director foundDirector = db.getDirector(dbconn, director);
        if (foundDirector == null) {
            foundDirector = db.createDirector(dbconn, director, null);
        }

        Integer intyear = null;
        if (!year.equals("")) {
            try {
                intyear = Integer.valueOf(year);
            } catch (NumberFormatException e) {
                System.out.println("An error has occurred while parsing 'year', storing as NULL.");
            }
        }
        Integer intruntime = null;
        if (!runtime.equals("")) {
            try {
                intruntime = Integer.valueOf(runtime);
            } catch (NumberFormatException e) {
                System.out.println("An error has occurred while parsing 'runtime', storing as NULL.");
            }
        }
        String [] genres = {};
        if (!genre.equals("")) {
            genres = genre.split(",");
            for (int i = 0; i < genres.length; i++) {
                genres[i] = this.toTitleCase(genres[i]).strip();
            }
        }
        if (!title.equals("") && title.length() >= 2 && foundDirector != null) {
            db.createMovie(dbconn, foundDirector, title, intyear, intruntime, genres);
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
                        repo.filterMovies(conn, "id");
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

    public void updateMovie(Scanner scanner) {
        String userInput = "";
        Repository db = new Repository();
        Connection dbconn = db.connectToDatabase("moviedb", "postgres", "!Jmjm1859");
        ArrayList<Movie> movieList = db.pullMovies(dbconn);
        Movie currentMovie = null;
        int page = 0;
        while (userInput != "Back") {
            for (int i = page * 5; i < Math.min(page * 5 + 5, movieList.size()); i++) {
                System.out.printf("%s: %s by %s\n", i + 1, movieList.get(i).title, movieList.get(i).director != null ? movieList.get(i).director.name : "Unknown Director");
            }
            System.out.printf("[<<][<]            %s/%s            [>][>>]\n", page + 1, movieList.size() % 5 == 0 ? Math.round(movieList.size() / 5) : Math.round(movieList.size() / 5) + 1);
            System.out.print("Enter the number/name of the movie you want to edit, or [Back] to cancel.\n> ");
            userInput = this.toTitleCase(scanner.nextLine().strip());
            switch (userInput) {
                case ">":
                    page = (page + 1) * 5 < movieList.size() ? page + 1 : page;
                    break;
                case "<":
                    page = Math.max(page - 1, 0);
                    break;
                case ">>":
                    page = movieList.size() % 5 == 0 ? Math.round(movieList.size() / 5) - 1 : Math.round(movieList.size() / 5);
                    break;
                case "<<":
                    page = 0;
                    break;
                case "Back":
                    return;
                default:
                    try {
                        int indexInput = Integer.parseInt(userInput) - 1;
                        if (indexInput >= 0 && indexInput < movieList.size()) {
                            currentMovie = db.getMovie(dbconn, movieList.get(indexInput).id);
                        }
                    } catch (NumberFormatException e) {
                        for (Movie m : movieList) {
                            if (m.title.equalsIgnoreCase(userInput)) {
                                currentMovie = db.getMovie(dbconn, m.id);
                            }
                        }
                    }
            }

            if (currentMovie != null) {
                System.out.printf("Updating %s\nTitle (currently %s): ", currentMovie.title, currentMovie.title);
                String title = toTitleCase(scanner.nextLine().strip());
                System.out.printf("Director (Name, currently %s): ", currentMovie.director.name);
                String director = toTitleCase(scanner.nextLine().strip());
                System.out.printf("Year (YYYY, currently %s): ", currentMovie.year);
                String year = scanner.nextLine().strip();
                System.out.printf("Runtime (in Minutes, currently %s): ", currentMovie.runtime);
                String runtime = scanner.nextLine().strip();
                System.out.printf("Genre (separated by ',' comma, currently %s): ", Arrays.stream(currentMovie.genre).toArray().toString());
                String genre = scanner.nextLine().strip();

                Director foundDirector = currentMovie.director;
                if (!director.equals("")) {
                    foundDirector = db.getDirector(dbconn, director);
                    if (foundDirector == null) {
                        foundDirector = db.createDirector(dbconn, this.toTitleCase(director), null);
                    }
                }

                Integer intyear = currentMovie.year;
                if (!year.equals("")) {
                    try {
                        intyear = Integer.valueOf(year);
                    } catch (NumberFormatException e) {
                        System.out.println("An error has occurred while parsing 'year'; reverting to current.");
                    }
                }

                Integer intruntime = currentMovie.runtime;
                if (!runtime.equals("")) {
                    try {
                        intruntime = Integer.valueOf(runtime);
                    } catch (NumberFormatException e) {
                        System.out.println("An error has occurred while parsing 'runtime'; reverting to current.");
                    }
                }

                String [] genres = currentMovie.genre;
                if (!genre.equals("")) {
                    genres = genre.split(",");
                    for (int i = 0; i < genres.length; i++) {
                        genres[i] = this.toTitleCase(genres[i]).strip();
                    }
                }
                db.updateMovie(dbconn, currentMovie, foundDirector, title, intyear, intruntime, genres);
                currentMovie = null;
                movieList = db.pullMovies(dbconn);
            }
        }
    }

    public void deleteMovies(Scanner scanner) {
        String userInput = "";
        Repository db = new Repository();
        Connection dbconn = db.connectToDatabase("moviedb", "postgres", "!Jmjm1859");
        ArrayList<Movie> movieList = db.pullMovies(dbconn);
        Movie currentMovie = null;
        int page = 0;
        while (userInput != "Back") {
            for (int i = page * 5; i < Math.min(page * 5 + 5, movieList.size()); i++) {
                System.out.printf("%s: %s by %s\n", i + 1, movieList.get(i).title, movieList.get(i).director.name);
            }
            System.out.printf("[<<][<]            %s/%s            [>][>>]\n", page + 1, movieList.size() % 5 == 0 ? Math.round(movieList.size() / 5) : Math.round(movieList.size() / 5) + 1);
            System.out.print("Enter the number/name of the movie you want to delete, or [Back] to cancel.\n> ");
            userInput = this.toTitleCase(scanner.nextLine().strip());
            switch (userInput) {
                case ">":
                    page = (page + 1) * 5 < movieList.size() ? page + 1 : page;
                    break;
                case "<":
                    page = Math.max(page - 1, 0);
                    break;
                case ">>":
                    page = movieList.size() % 5 == 0 ? Math.round(movieList.size() / 5) - 1 : Math.round(movieList.size() / 5);
                    break;
                case "<<":
                    page = 0;
                    break;
                case "Back":
                    return;
                default:
                    try {
                        int indexInput = Integer.parseInt(userInput) - 1;
                        if (indexInput >= 0 && indexInput < movieList.size()) {
                            currentMovie = db.getMovie(dbconn, movieList.get(indexInput).id);
                        }
                    } catch (NumberFormatException e) {
                        for (Movie m : movieList) {
                            if (m.title.equalsIgnoreCase(userInput)) {
                                currentMovie = db.getMovie(dbconn, m.id);
                            }
                        }
                    }
            }

            if (currentMovie != null) {
                db.deleteMovie(dbconn, currentMovie);
                currentMovie = null;
                movieList = db.pullMovies(dbconn);
            }
        }
    }

    public String toTitleCase(String title) {
        boolean space = true;
        for (int i = 0; i < title.length(); i++) {
            if (space) {
                if (i == 0 && title.length() > 1) {
                    title = String.format("%s", title.charAt(0)).toUpperCase() + title.substring(1);
                } else if (i == 0 && title.length() == 1) {
                    title = title.toUpperCase();
                } else {
                    title = title.substring(0, i) + String.format("%s", title.charAt(i)).toUpperCase() + title.substring(i + 1);
                }

            } else {
                if (title.length() > 2) {
                    title = title.substring(0, i) + String.format("%s", title.charAt(i)).toLowerCase() + title.substring(i + 1);
                } else {
                    title = title.substring(0, i) + String.format("%s", title.charAt(i)).toLowerCase();
                }
            }

            if (title.charAt(i) == ' '){
                if (space) {
                    title = title.substring(0, i) + title.substring(i+1);
                    i--;
                }
                space = true;
            } else {
                space = false;
            }
        }
        return title;
    }
}