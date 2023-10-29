import java.util.HashSet;

public class Movie {
    private String title;
    private String year;
    private String director;
    private HashSet<String> genres;

    public Movie() {

    }

    public Movie(String title, String year, String director) {
        this.title = title;
        this.year = year;
        this.director = director;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getYear() {
        return year;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getDirector() {
        return director;
    }

    public void setGenres(HashSet<String> genres) {
        this.genres = genres;
    }

    public HashSet<String> getGenres() {
        return genres;
    }
}
