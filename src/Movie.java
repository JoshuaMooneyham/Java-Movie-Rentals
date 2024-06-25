import java.util.ArrayList;

public class Movie {
    public int id;
    public Director director;
    public String title;
    public int year;
    public int runtime;
    public String[] genre = {};

    public Movie(int id, Director director, String title, int year, int runtime, String[] genre) {
        this.id = id;
        this.director = director;
        this.title = title;
        this.year = year;
        this.runtime = runtime;
        this.genre = genre;
    }

    public String toString() {
        return String.format("Movie %s: %s", this.id, this.title);
    }
}
