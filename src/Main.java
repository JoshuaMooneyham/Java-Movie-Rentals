import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    private Account currentUser;
    private boolean running = true;
    private boolean adminView;
    private final Repository db = new Repository();
    private int page;

    public static void main(String[] args) {
        new Main().mainProgramLoop();
    }

    public void mainProgramLoop() {
        Scanner sc = new Scanner(System.in);
        String userInput;

        while (this.running) {
            if (this.currentUser == null) {
                Account acc = null;
                System.out.println("Welcome! Would you like to [Log In], [Create] Account, or [Q]uit?");
                System.out.print("> ");
                userInput = sc.nextLine().toLowerCase().strip();
                switch (userInput) {
                    case "log in":
                    case "login":
                        acc = this.login(sc);
                        break;
                    case "create":
                        acc = this.createAccount(sc);
                        break;
                    case "q":
                    case "quit":
                        this.quit(sc);
                        break;
                    default:
                        System.out.println("Please enter a valid action.");
                        break;
                }
                if (acc != null) {
                    this.currentUser = acc;
                    this.adminView = this.currentUser.isAdmin;
                }
            } else if (this.adminView) {
                System.out.printf("Welcome %s, what would you like to do today?\n", this.currentUser.username);
                System.out.println("Manage [Movies], Manage [Users], Manage [Directors], View as [Customer], or [Log out]");
                System.out.print("> ");
                userInput = sc.nextLine().toLowerCase().strip();
                switch (userInput) {
                    case "movies":
                        this.manageMovies(sc);
                        break;
                    case "users":
                        this.manageUsers(sc);
                        break;
                    case "directors":
                        this.manageDirectors(sc);
                        break;
                    case "customer":
                        this.adminView = false;
                        break;
                    case "logout":
                    case "log out":
                        this.logout();
                        break;
                    default:
                        System.out.println("Please enter a valid input.");
                        break;
                }
            } else {
                System.out.printf("Welcome %s, what would you like to do today?\n", this.currentUser.username);
                System.out.printf("[View] movies, [Rent] movies, [Return] rented movies, %sor [Logout]\n", this.currentUser.isAdmin ? "[Admin] view, " : "");
                System.out.print("> ");
                userInput = sc.nextLine().toLowerCase().strip();
                switch (userInput) {
                    case "view":
                        this.viewMovies(sc);
                        break;
                    case "logout":
                        this.logout();
                        break;
                    case "rent":
                        this.rentMovies(sc);
                        break;
                    case "return":
                        this.returnMovies(sc);
                        break;
                    case "admin":
                        if (this.currentUser.isAdmin) {
                            this.adminView = true;
                            break;
                        }
                    default:
                        System.out.println("Please enter a valid input.");
                }
            }
        }
    }

    private Account login(Scanner scanner) {
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        try {
            Account found_account = this.db.getAccount(username, password);
            if (found_account == null) {
                System.out.println("Could not find any account matching that Username/Password combination.");
            }
            return found_account;

        } catch (Exception e) {
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
            return this.db.createAccount(username, password, false);
        } else {
            System.out.println("Error: Passwords must match!");
            return null;
        }
    }

    public void logout() {
        this.currentUser = null;
    }
// Paginated
    public void returnMovies(Scanner scanner) {
        String userInput = "";
        ArrayList<Movie> movieList = this.db.rentedMovies(this.currentUser);
        Movie currentMovie = null;
        this.page = 0;
        while (!movieList.isEmpty() && userInput != null) {
            userInput = paginateMovies(scanner, movieList, "return", 5);

            try {
                int indexInput = Integer.parseInt(userInput) - 1;
                if (indexInput >= 0 && indexInput < movieList.size()) {
                    currentMovie = movieList.get(indexInput);
                }
            } catch (NumberFormatException e) {
                for (Movie m : movieList) {
                    if (m.title.equalsIgnoreCase(userInput)) {
                        currentMovie = m;
                    }
                }
            }

            if (currentMovie != null) {
                this.db.returnMovie(currentMovie);
                movieList = this.db.rentedMovies(this.currentUser);
            }
        }

        if (movieList.isEmpty()) {
            System.out.println("You don't have any rented movies!");
        }
    }
// Paginated
    public void rentMovies(Scanner scanner) {
        String userInput = "";
        ArrayList<Movie> movieList = this.db.availableMovies();
        Movie currentMovie = null;
        this.page = 0;
        while (!movieList.isEmpty() && userInput != null) {

            userInput = this.paginateMovies(scanner, movieList, "rent", 5);

            try {
                int indexInput = Integer.parseInt(userInput) - 1;
                if (indexInput >= 0 && indexInput < movieList.size()) {
                    currentMovie = movieList.get(indexInput);
                }
            } catch (NumberFormatException e) {
                for (Movie m : movieList) {
                    if (m.title.equalsIgnoreCase(userInput)) {
                        currentMovie = m;
                    }
                }
            }

            if (currentMovie != null && userInput != null) {
                this.db.rentMovie(this.currentUser, currentMovie);
                movieList = this.db.availableMovies();
            }
        }

        if (movieList.isEmpty()) {
            System.out.println("All movies are currently unavailable!");
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

    public String paginateMovies(Scanner scanner, ArrayList<Movie> movieList, String task, int amount) {
        String userInput = "";
        while (!userInput.equals("Back")) {
            if ((this.page) * amount > movieList.size()) {
                this.page--;
            }

            for (int i = this.page * amount; i < Math.min(this.page * amount + amount, movieList.size()); i++) {
                System.out.printf("%s: %s by %s\n", i + 1, movieList.get(i).title, movieList.get(i).director != null ? movieList.get(i).director.name : "Unknown Director");
            }
            System.out.printf("[<<][<]            %s/%s            [>][>>]\n", this.page + 1, movieList.size() % amount == 0 ? Math.round(movieList.size() / amount) : Math.round(movieList.size() / amount) + 1);
            System.out.printf("Enter the number/name of the movie you want to %s, or [Back] to cancel.\n> ", task);
            userInput = this.toTitleCase(scanner.nextLine().strip());
            switch (userInput) {
                case ">":
                    this.page = (this.page + 1) * amount < movieList.size() ? this.page + 1 : this.page;
                    break;
                case "<":
                    this.page = Math.max(this.page - 1, 0);
                    break;
                case ">>":
                    this.page = movieList.size() % amount == 0 ? Math.round(movieList.size() / amount) - 1 : Math.round(movieList.size() / amount);
                    break;
                case "<<":
                    this.page = 0;
                    break;
                case "Back":
                    break;
                default:
                    return userInput;
            }
        }
        return null;
    }

    public String paginateUsers(Scanner scanner, ArrayList<Account> accountList, String task, int amount) {
        String userInput = "";
        while (!userInput.equals("Back")) {
            if ((this.page) * amount > accountList.size()) {
                this.page--;
            }

            for (int i = this.page * amount; i < Math.min(this.page * amount + amount, accountList.size()); i++) {
                System.out.printf("%s: %s\n", i + 1, accountList.get(i).username);
            }
            System.out.printf("[<<][<]            %s/%s            [>][>>]\n", this.page + 1, accountList.size() % amount == 0 ? Math.round(accountList.size() / amount) : Math.round(accountList.size() / amount) + 1);
            System.out.printf("Enter the number/name of the user you want to %s, or [Back] to cancel.\n> ", task);
            userInput = this.toTitleCase(scanner.nextLine().strip());
            switch (userInput) {
                case ">":
                    this.page = (this.page + 1) * amount < accountList.size() ? this.page + 1 : this.page;
                    break;
                case "<":
                    this.page = Math.max(this.page - 1, 0);
                    break;
                case ">>":
                    this.page = accountList.size() % amount == 0 ? Math.round(accountList.size() / amount) - 1 : Math.round(accountList.size() / amount);
                    break;
                case "<<":
                    this.page = 0;
                    break;
                case "Back":
                    break;
                default:
                    return userInput;
            }
        }
        return null;
    }

    public String paginateDirectors(Scanner scanner, ArrayList<Director> directorList, String task, int amount) {
        String userInput = "";
        while (!userInput.equals("Back")) {
            if ((this.page) * amount > directorList.size()) {
                this.page--;
            }

            for (int i = this.page * amount; i < Math.min(this.page * amount + amount, directorList.size()); i++) {
                System.out.printf("%s: %s\n", i + 1, directorList.get(i).name);
            }
            System.out.printf("[<<][<]            %s/%s            [>][>>]\n", this.page + 1, directorList.size() % amount == 0 ? Math.round(directorList.size() / amount) : Math.round(directorList.size() / amount) + 1);
            System.out.printf("Enter the number/name of the director you want to %s, or [Back] to cancel.\n> ", task);
            userInput = this.toTitleCase(scanner.nextLine().strip());
            switch (userInput) {
                case ">":
                    this.page = (this.page + 1) * amount < directorList.size() ? this.page + 1 : this.page;
                    break;
                case "<":
                    this.page = Math.max(this.page - 1, 0);
                    break;
                case ">>":
                    this.page = directorList.size() % amount == 0 ? Math.round(directorList.size() / amount) - 1 : Math.round(directorList.size() / amount);
                    break;
                case "<<":
                    this.page = 0;
                    break;
                case "Back":
                    break;
                default:
                    return userInput;
            }
        }
        return null;
    }

    public static void formatTables(ArrayList<Movie> movieList, int[] spaces) {
        for (Movie m : movieList) {
            m.title = m.title.length() > 25 ? m.title.substring(0, 22) + "..." : m.title;
            m.director.name = m.director.name.length() > 25 ? m.director.name.substring(0, 22) + "..." : m.director.name;
            spaces[0] = Math.max(spaces[0], m.title.length());
            spaces[1] = Math.max(spaces[1], m.director.name.length());
            spaces[2] = Math.max(spaces[2], String.format("%s", m.year).length());
            spaces[3] = Math.max(spaces[3], String.format("%s", m.runtime).length());
        }
        if (!movieList.isEmpty()) {
            String header = "\nAvailability |";
            header += String.format(" Title%s |", spaces[0] > 5 ? " ".repeat(spaces[0] - 5) : "");
            header += String.format(" Director%s |", spaces[1] > 8 ? " ".repeat(spaces[1] - 8) : "");
            header += String.format(" Year%s |", " ".repeat(spaces[2] - 4));
            header += String.format(" Runtime%s | Genre\n", spaces[3] > 7 ? " ".repeat(spaces[3] - 7) : "");
            System.out.println(header);
            for (Movie m : movieList) {
                int yearSpots = m.year > 0 ? String.format("%s", m.year).length() : 7;
                int runtimeSpots = m.runtime > 0 ? String.format("%s", m.runtime).length() : 7;
                StringBuilder row = new StringBuilder(String.format("%s |", m.rented_by > 0 ? "Unavailable " : "Available   "));
                row.append(String.format(" %s%s |", m.title, spaces[0] > m.title.length() ? " ".repeat(spaces[0] - m.title.length()) : ""));
                row.append(String.format(" %s%s |", m.director.name, spaces[1] > m.director.name.length() ? " ".repeat(spaces[1] - m.director.name.length()) : ""));
                row.append(String.format(" %s%s |", m.year > 0 ? m.year : "Unknown", spaces[2] > yearSpots ? " ".repeat(spaces[2] - yearSpots) : ""));
                row.append(String.format(" %s%s |", m.runtime > 0 ? m.runtime : "Unknown", spaces[3] > runtimeSpots ? " ".repeat(spaces[3] - runtimeSpots) : ""));
                for (String g : m.genre) {
                    row.append(String.format(" %s", g));
                }
                System.out.println(row);
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

        Director foundDirector = this.db.getDirector(director);
        if (foundDirector == null) {
            foundDirector = this.db.createDirector(director);
        }

        Integer intyear = null;
        if (!year.isEmpty()) {
            try {
                intyear = Integer.valueOf(year);
            } catch (NumberFormatException e) {
                System.out.println("An error has occurred while parsing 'year', storing as NULL.");
            }
        }
        Integer intruntime = null;
        if (!runtime.isEmpty()) {
            try {
                intruntime = Integer.valueOf(runtime);
            } catch (NumberFormatException e) {
                System.out.println("An error has occurred while parsing 'runtime', storing as NULL.");
            }
        }
        String [] genres = {};
        if (!genre.isEmpty()) {
            genres = genre.split(",");
            for (int i = 0; i < genres.length; i++) {
                genres[i] = this.toTitleCase(genres[i]).strip();
            }
        }
        if (!title.isEmpty() && title.length() >= 2 && foundDirector != null) {
            this.db.createMovie(foundDirector, title, intyear, intruntime, genres);
        }
    }

    public void viewMovies(Scanner scanner) {
        String userInput;
        while (true) {
            boolean skip = false;
            System.out.println("What would you like to search by?");
            System.out.println("[Genre], [Year], [Runtime], [Title], [Available] (or [Back] to exit)");
            System.out.print("> ");
            userInput = scanner.nextLine().toLowerCase().strip();
            if (!skip) {
                switch (userInput) {
                    case "back", "b":
                        return;
                    case "year", "runtime", "title":
                        this.db.filterMovies(userInput);
                        break;
                    case "available":
                        this.db.printAvailableMovies();
                        break;
                    case "genre":
                        System.out.print("Enter Genre: ");
                        String genre = this.toTitleCase(scanner.nextLine());
                        this.db.filterGenres(genre);
                        break;
                    default:
                        System.out.println("Please enter a valid action!");
                }
            }
        }
    }
// Paginated
    public void updateMovie(Scanner scanner) {
        String userInput = "";
        ArrayList<Movie> movieList = this.db.pullMovies();
        Movie currentMovie = null;
        this.page = 0;
        while (userInput != null) {
            userInput = this.paginateMovies(scanner, movieList, "edit", 5);

            try {
                int indexInput = Integer.parseInt(userInput) - 1;
                if (indexInput >= 0 && indexInput < movieList.size()) {
                    currentMovie = movieList.get(indexInput);
                }
            } catch (NumberFormatException e) {
                for (Movie m : movieList) {
                    if (m.title.equalsIgnoreCase(userInput)) {
                        currentMovie = m;
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
                System.out.printf("Genre (separated by ',' comma, currently %s): ", String.join(", ", currentMovie.genre));
                String genre = scanner.nextLine().strip();

                if (title.length() < 2) {
                    if (!title.isEmpty()) {
                        System.out.println("An error has occurred while parsing 'title'; reverting to current.");
                    }
                    title = currentMovie.title;
                }

                Director foundDirector = currentMovie.director;
                if (!director.isEmpty()) {
                    foundDirector = this.db.getDirector(director);
                    if (foundDirector == null) {
                        foundDirector = this.db.createDirector(this.toTitleCase(director));
                    }
                }

                Integer intyear = currentMovie.year;
                if (!year.isEmpty()) {
                    try {
                        intyear = Integer.valueOf(year);
                    } catch (NumberFormatException e) {
                        System.out.println("An error has occurred while parsing 'year'; reverting to current.");
                    }
                }

                Integer intruntime = currentMovie.runtime;
                if (!runtime.isEmpty()) {
                    try {
                        intruntime = Integer.valueOf(runtime);
                    } catch (NumberFormatException e) {
                        System.out.println("An error has occurred while parsing 'runtime'; reverting to current.");
                    }
                }

                String [] genres = currentMovie.genre;
                if (!genre.isEmpty()) {
                    genres = genre.split(",");
                    for (int i = 0; i < genres.length; i++) {
                        genres[i] = this.toTitleCase(genres[i]).strip();
                    }
                }

                this.db.updateMovie(currentMovie, foundDirector, title, intyear, intruntime, genres);
                currentMovie = null;
                movieList = this.db.pullMovies();
            }
        }
    }
// Paginated
    public void deleteMovies(Scanner scanner) {
        String userInput = "";
        ArrayList<Movie> movieList = this.db.pullMovies();
        Movie currentMovie = null;
        this.page = 0;
        while (userInput != null) {

            userInput = this.paginateMovies(scanner, movieList, "delete", 5);

            try {
                int indexInput = Integer.parseInt(userInput) - 1;
                if (indexInput >= 0 && indexInput < movieList.size()) {
                    currentMovie = movieList.get(indexInput);
                }
            } catch (NumberFormatException e) {
                for (Movie m : movieList) {
                    if (m.title.equalsIgnoreCase(userInput)) {
                        currentMovie = m;
                    }
                }
            }

            if (currentMovie != null) {
                this.db.deleteMovie(currentMovie);
                currentMovie = null;
                movieList = this.db.pullMovies();
            }
        }
    }

    public void manageUsers(Scanner scanner) {
        String userInput;
        while (true) {
            System.out.println("[View] all users, [Promote] users, [Demote] users, [Delete] users, or [Back]");
            System.out.print("> ");
            userInput = scanner.nextLine().toLowerCase().strip();
            switch (userInput) {
                case "promote":
                    this.promoteUser(scanner);
                    break;
                case "view":
                    ArrayList<Account> accList = this.db.pullUsers();
                    for (Account a : accList) {
                        System.out.printf("%s (%s)\n", a.username, a.isAdmin ? "Admin" : "Customer");
                    }
                    break;
                case "delete":
                    this.deleteUsers(scanner);
                    break;
                case "demote":
                    this.demoteUser(scanner);
                    break;
                case "back":
                    return;
                default:
                    System.out.println("Please enter a valid input.");
            }
        }
    }
// Paginated
    public void promoteUser(Scanner scanner) {
        String userInput = "";
        ArrayList<Account> accountList = this.db.pullCustomers();
        Account currentAccount = null;
        this.page = 0;
        while (userInput != null) {

            userInput = this.paginateUsers(scanner, accountList, "promote", 5);

            try {
                int indexInput = Integer.parseInt(userInput) - 1;
                if (indexInput >= 0 && indexInput < accountList.size()) {
                    currentAccount = accountList.get(indexInput);
                }
            } catch (NumberFormatException e) {
                for (Account acc : accountList) {
                    if (acc.username.equalsIgnoreCase(userInput)) {
                        currentAccount = acc;
                    }
                }
            }

            if (currentAccount != null) {
                this.db.promoteCustomer(currentAccount);
                currentAccount = null;
                accountList = this.db.pullCustomers();
            }
        }
    }
// Paginated
    public void demoteUser(Scanner scanner) {
        String userInput = "";
        ArrayList<Account> accountList = this.db.pullAdmin();
        Account currentAccount = null;
        this.page = 0;
        while (userInput != null) {

            userInput = this.paginateUsers(scanner, accountList, "demote", 5);

            try {
                int indexInput = Integer.parseInt(userInput) - 1;
                if (indexInput >= 0 && indexInput < accountList.size()) {
                    currentAccount = accountList.get(indexInput);
                }
            } catch (NumberFormatException e) {
                for (Account acc : accountList) {
                    if (acc.username.equalsIgnoreCase(userInput)) {
                        currentAccount = acc;
                    }
                }
            }

            if (currentAccount != null) {
                this.db.demoteAdmin(currentAccount);
                currentAccount = null;
                accountList = this.db.pullAdmin();
            }
        }
    }
// Paginated
    public void deleteUsers(Scanner scanner) {
        String userInput = "";
        ArrayList<Account> accountList = this.db.pullUsers();
        Account currentAccount = null;
        this.page = 0;
        while (userInput != null) {

            userInput = this.paginateUsers(scanner, accountList, "delete", 5);

            try {
                int indexInput = Integer.parseInt(userInput) - 1;
                if (indexInput >= 0 && indexInput < accountList.size()) {
                    currentAccount = accountList.get(indexInput);
                }
            } catch (NumberFormatException e) {
                for (Account acc : accountList) {
                    if (acc.username.equalsIgnoreCase(userInput)) {
                        currentAccount = acc;
                    }
                }
            }

            if (currentAccount != null) {
                this.db.deleteUser(currentAccount);
                currentAccount = null;
                accountList = this.db.pullUsers();
            }
        }
    }

    public void manageDirectors(Scanner scanner) {
        String userInput;
        while (true) {
            System.out.println("[Add] new director, [Delete] directors, [Update] directors, [View] directors, or [Back]");
            System.out.print("> ");
            userInput = scanner.nextLine().toLowerCase().strip();
            switch (userInput) {
                case "add":
                    this.addDirector(scanner);
                    break;
                case "view":
                    this.viewDirectors(scanner);
                    break;
                case "delete":
                    this.deleteDirectors(scanner);
                    break;
                case "update":
                    this.updateDirectors(scanner);
                    break;
                case "back":
                    return;
                default:
                    System.out.println("Please enter a valid input.");
            }
        }
    }

    public void addDirector(Scanner scanner) {
        String name = "";
        System.out.print("Name (or [Back] to cancel): ");
        while (name.length() < 2) {
            name = this.toTitleCase(scanner.nextLine().strip());
            if (name.equals("Back")) {
                return;
            } else if (name.length() < 2) {
                System.out.print("Please enter a valid title or [Back]: ");
            }
        }

        if (!name.isEmpty() && name.length() >= 2) {
            this.db.createDirector(name);
        }
    }

    public void viewDirectors(Scanner scanner) {
        ArrayList<Director> directorList = this.db.pullDirectors();
        ArrayList<Movie> movieList = this.db.pullMovies();
        for (Director d : directorList) {
            System.out.printf("%s\n", d.name);
            this.db.printDirectorMovies(d);
        }
    }
// Paginated
    public void updateDirectors(Scanner scanner) {
        String userInput = "";
        ArrayList<Director> directorList = this.db.pullDirectors();
        Director currentDirector = null;
        this.page = 0;
        while (userInput != null) {

            userInput = this.paginateDirectors(scanner, directorList, "update", 5);

            try {
                int indexInput = Integer.parseInt(userInput) - 1;
                if (indexInput >= 0 && indexInput < directorList.size()) {
                    currentDirector = directorList.get(indexInput);
                }
            } catch (NumberFormatException e) {
                for (Director d : directorList) {
                    if (d.name.equalsIgnoreCase(userInput)) {
                        currentDirector = d;
                    }
                }
            }

            if (currentDirector != null) {
                System.out.printf("Updating %s\nName (currently %s): ", currentDirector.name, currentDirector.name);
                String name = this.toTitleCase(scanner.nextLine().strip());

                if (name.length() < 2) {
                    name = currentDirector.name;
                    if (!name.isEmpty()) {
                        System.out.println("An error has occurred while parsing 'title'; reverting to current.");
                    }
                } else {
                    currentDirector.name = name;
                }

                this.db.updateDirector(currentDirector);
                currentDirector = null;
                directorList = this.db.pullDirectors();
            }
        }
    }
// Paginated
    public void deleteDirectors(Scanner scanner) {
        String userInput = "";
        ArrayList<Director> directorList = this.db.pullDirectors();
        Director currentDirector = null;
        this.page = 0;
        while (userInput != null) {

            userInput = this.paginateDirectors(scanner, directorList, "delete", 5);

            try {
                int indexInput = Integer.parseInt(userInput) - 1;
                if (indexInput >= 0 && indexInput < directorList.size()) {
                    currentDirector = directorList.get(indexInput);
                }
            } catch (NumberFormatException e) {
                for (Director d : directorList) {
                    if (d.name.equalsIgnoreCase(userInput)) {
                        currentDirector = d;
                    }
                }
            }

            if (currentDirector != null) {
                this.db.deleteDirector(currentDirector);
                currentDirector = null;
                directorList = this.db.pullDirectors();
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