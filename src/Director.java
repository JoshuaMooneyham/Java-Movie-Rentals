public class Director {
    public int id;
    public String name;
    public int age;

    public Director(int id, String name, int age) {
        this.id = id;
        this.name=name;
        this.age=age;
    }

    public String toString() {
        return String.format("Director %s: %s", this.id, this.name);
    }
}
