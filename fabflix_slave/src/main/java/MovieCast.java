import java.util.HashSet;

public class MovieCast {
    private String movieTitle;
    private String director;
    private HashSet<String> cast;

    public MovieCast() {

    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getDirector() {
        return director;
    }

    public void setCast(HashSet<String> cast) {
        this.cast = cast;
    }

    public HashSet<String> getCast() {
        return cast;
    }
}
