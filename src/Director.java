public class Director {
    public int id;
    public String name;

    public Director(int id, String name) {
        this.id = id;
        this.name=name;
    }

    public String toString() {
        return String.format("Director %s: %s", this.id, this.name);
    }
}
