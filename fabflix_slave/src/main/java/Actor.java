public class Actor {
    private String name;
    private String birthYear;

    public Actor() {
    }

    public Actor(String name, String birthYear) {
        this.name = name;
        this.birthYear = birthYear;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setBirthYear(String birthYear) {
        this.birthYear = birthYear;
    }

    public String getBirthYear() {
        return birthYear;
    }
}
