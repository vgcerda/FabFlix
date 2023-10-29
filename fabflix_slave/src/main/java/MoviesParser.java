import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.sql.*;
//import javax.sql.DataSource;

public class MoviesParser extends DefaultHandler {
    private static FileWriter file;
    private long startTime;
    private String errors;
    private int duplicates;
    private int inserted;
    private int skipped;

    List<Movie> movies;

    HashSet<String> moviesFromMoviesTable;

    HashMap<String, String> genreCodeMap;
    HashMap<String, Integer> genresFromGenresTable;

    private String tempString;
    private String tempDirector;
    private Movie tempMovie;
    HashSet<String> tempGenres;

    private int movieIdReference;
    private int genreIdReference;

    private Connection dbcon;
//    private DataSource dataSource;

    public MoviesParser() {
        startTime = System.nanoTime();
        errors = "";
        duplicates = 0;
        inserted = 0;
        skipped = 0;

        movies = new ArrayList<>();
        moviesFromMoviesTable = new HashSet<>();
        genresFromGenresTable = new HashMap<>();
        tempGenres = new HashSet<>();
        genreCodeMap = new HashMap<>();

        try {
            String jdbcURL="jdbc:mysql://localhost:3306/moviedb";
            dbcon = DriverManager.getConnection(jdbcURL, "mytestuser", "Team!!50");

            Statement s = dbcon.createStatement();

            ResultSet lastMovieIdResult = s.executeQuery("SELECT MAX(id) last_id FROM movies;");
            while (lastMovieIdResult.next()) {
                movieIdReference = Integer.parseInt(lastMovieIdResult.getString("last_id").substring(2));
            }
            lastMovieIdResult.close();

            ResultSet lastGenreIdResult = s.executeQuery("SELECT MAX(id) last_id FROM genres;");
            while (lastGenreIdResult.next()) {
                genreIdReference = lastGenreIdResult.getInt("last_id");
            }
            lastGenreIdResult.close();

            ResultSet moviesResult = s.executeQuery("SELECT title, year, director FROM movies;");
            String temp = "";
            while (moviesResult.next()) {
                temp = moviesResult.getString("title") + "/";
                temp += moviesResult.getInt("year") + "/";
                temp += moviesResult.getString("director");
                moviesFromMoviesTable.add(temp);
            }
            moviesResult.close();
            String tempKey = "";
            int tempVal = 0;
            ResultSet genresResult = s.executeQuery("SELECT id, name FROM genres;");
            while (genresResult.next()) {
                tempKey = genresResult.getString("name");
                tempVal = genresResult.getInt("id");
                genresFromGenresTable.put(tempKey, tempVal);
            }
            genresResult.close();
            s.close();

            genreCodeMap.put("susp", "Thriller");
            genreCodeMap.put("cnr", "Cops and Robbers");
            genreCodeMap.put("dram", "Drama");
            genreCodeMap.put("west", "Western");
            genreCodeMap.put("myst", "Mystery");
            genreCodeMap.put("s.f.", "Science Fiction");
            genreCodeMap.put("advt", "Adventure");
            genreCodeMap.put("horr", "Horror");
            genreCodeMap.put("romt", "Romance");
            genreCodeMap.put("comd", "Comedy");
            genreCodeMap.put("musc", "Musical");
            genreCodeMap.put("docu", "Documentary");
            genreCodeMap.put("porn", "Adult");
            genreCodeMap.put("noir", "Black");
            genreCodeMap.put("biop", "Biographical Picture");
            genreCodeMap.put("tv", "TV Show");
            genreCodeMap.put("tvs", "TV Series");
            genreCodeMap.put("tvm", "TV Miniseries");
            genreCodeMap.put("actn", "Action");
            genreCodeMap.put("disa", "Disaster");
            genreCodeMap.put("epic", "Epic");
            genreCodeMap.put("scifi", "Science Fiction");
            genreCodeMap.put("avga", "Avant Garde");
            genreCodeMap.put("hist", "History");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {

        parseDocument();
        insertMovies();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("Parsing Main XML took: " + duration + "ms.");
        System.out.println("Duplicates Found: " + duplicates);
        System.out.println("Entries Inserted: " + inserted);
        System.out.println("Entries Skipped: " + skipped);
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
//            /home/ubuntu/mains243.xml

            sp.parse("/home/ubuntu/mains243.xml", this);

        } catch (SAXException se) {
            se.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    /**
     * Iterate through the list and print
     * the contents
     */
    private void insertMovies() {
        try {
            file = new FileWriter("main243_inconsistencies.txt");

            dbcon.setAutoCommit(false);

            String insertMovieQuery = "INSERT INTO movies values (?, ?, ?, ?)";
            String insertGenreQuery = "INSERT INTO genres values (?, ?)";
            String insertGenreInMovieQuery = "INSERT INTO genres_in_movies values (?, ?)";

            PreparedStatement psInsertMovie = dbcon.prepareStatement(insertMovieQuery);
            PreparedStatement psInsertGenre = dbcon.prepareStatement(insertGenreQuery);
            PreparedStatement psInsertGenreInMovieQuery = dbcon.prepareStatement(insertGenreInMovieQuery);

            Iterator<Movie> it = movies.iterator();
            while (it.hasNext()) {
                Movie m = it.next();
                String title = m.getTitle().trim();
                String year = m.getYear();
                String director = m.getDirector().trim();
                HashSet<String> genres = m.getGenres();

                boolean validYear = true;
                try {
                    if (year != null) {
                        year = year.trim();
                    }
                    Integer.parseInt(year);
                } catch (NumberFormatException e) {
                    if (year == null || !year.equals("")) {
                        System.out.println(String.format("Film entry with title '%s' and director '%s' has inconsistent year format (year = %s)", title, director, year));
                        errors += String.format("Film entry with title '%s' and director '%s' has inconsistent year format (year = %s)", title, director, year) + "\n";
                    }
                    validYear = false;
                    year = "";
                }

                String check = title + "/" + year + "/" + director;
                if (!moviesFromMoviesTable.contains(check)) {
                    movieIdReference++;
                    String movieId = "";
                    if (Integer.toString(movieIdReference).length() < 7) {
                        movieId += "tt0" + movieIdReference;
                    }else{
                        movieId += "tt" + movieIdReference;
                    }
                    psInsertMovie.setString(1, movieId);
                    psInsertMovie.setString(2, title);
                    if (validYear) {
                        psInsertMovie.setInt(3, Integer.parseInt(year));
                    }
                    else {
                        psInsertMovie.setInt(3, 0);
                    }
                    psInsertMovie.setString(4, director);
                    psInsertMovie.addBatch();

                    if (genres != null && genres.size() != 0) {
                        inserted++;
                        Iterator<String> genreIt = genres.iterator();
                        while (genreIt.hasNext()) {
                            String genre = genreIt.next().trim().toLowerCase();
                            if (genreCodeMap.containsKey(genre)) {
                                String genreCodeTranslated = genreCodeMap.get(genre);
                                if (!genresFromGenresTable.containsKey(genreCodeTranslated)) {
                                    genreIdReference++;
                                    psInsertGenre.setInt(1, genreIdReference);
                                    psInsertGenre.setString(2, genreCodeTranslated);
                                    psInsertGenre.addBatch();
                                    genresFromGenresTable.put(genreCodeTranslated, genreIdReference);
                                }
                                psInsertGenreInMovieQuery.setInt(1, genresFromGenresTable.get(genreCodeTranslated));
                                psInsertGenreInMovieQuery.setString(2, movieId);
                                psInsertGenreInMovieQuery.addBatch();
                            }
                            else {
                                System.out.println(String.format("Film entry with title '%s' , year '%s', and director '%s' has inconsistent genre code format (cat = %s)", title, year, director, genre));
                                errors += String.format("Film entry with title '%s' , year '%s', and director '%s' has inconsistent genre code format (cat = %s)", title, year, director, genre) + "\n";
                            }
                        }
                    }
                    else {
                        skipped++;
                        System.out.println(String.format("Film entry with title '%s' , year '%s', and director '%s' has no genre", title, year, director));
                        errors += String.format("Film entry with title '%s' , year '%s', and director '%s' has no genre", title, year, director) + "\n";
                    }
                    moviesFromMoviesTable.add(check);
                }
                else {
                    duplicates++;
                }
            }

            file.write(errors);

            psInsertMovie.executeBatch();
            psInsertGenre.executeBatch();
            psInsertGenreInMovieQuery.executeBatch();
            dbcon.commit();
            dbcon.close();
            psInsertMovie.close();
            psInsertGenre.close();
            psInsertGenreInMovieQuery.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    private void naiveInsertMovies() {
//        try {
//            String insertMovieQuery = "INSERT INTO movies values (?, ?, ?, ?)";
//            String insertGenreQuery = "INSERT INTO genres values (?, ?)";
//            String insertGenreInMovieQuery = "INSERT INTO genres_in_movies values (?, ?)";
//
//            PreparedStatement ps = null;
//            PreparedStatement psForExistsCheck = null;
//
//            Iterator<Actor> it = actors.iterator();
//            while (it.hasNext()) {
//                Actor a = it.next();
//                String name = a.getName();
//                String birthYear = a.getBirthYear();
//                boolean hasBirthYear = true;
//                try {
//                    Integer.parseInt(birthYear);
//                } catch (NumberFormatException e) {
//                    hasBirthYear = false;
//                }
//
//                String actorExistsQuery = "";
//
//                if (hasBirthYear) {
//                    actorExistsQuery = "SELECT EXISTS(SELECT * FROM stars WHERE name = ? AND birthYear = ?) actor_exists;";
//                    psForExistsCheck = dbcon.prepareStatement(actorExistsQuery);
//                    psForExistsCheck.setString(1, name);
//                    psForExistsCheck.setInt(2, Integer.parseInt(birthYear));
//                }
//                else {
//                    actorExistsQuery = "SELECT EXISTS(SELECT * FROM stars WHERE name = ? AND birthYear IS NULL) actor_exists;";
//                    psForExistsCheck = dbcon.prepareStatement(actorExistsQuery);
//                    psForExistsCheck.setString(1, name);
//                }
//
//                ResultSet actorExistsQueryResult = psForExistsCheck.executeQuery();
//
//                while (actorExistsQueryResult.next()) {
//                    boolean actorExists = actorExistsQueryResult.getBoolean("actor_exists");
//                    if (!actorExists) {
//                        actorIdReference++;
//                        ps = dbcon.prepareStatement(insertActorQuery);
//                        ps.setString(1, "nm" + actorIdReference);
//                        ps.setString(2, name);
//                        if (!hasBirthYear) {
//                            ps.setNull(3, Types.NULL);
//                        }
//                        else{
//                            ps.setInt(3, Integer.parseInt(birthYear));
//                        }
//                        ps.execute();
//                    }
//                }
//
//            }
//
//            dbcon.close();
//            ps.close();
//            psForExistsCheck.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempString = "";
        if (qName.equalsIgnoreCase("Film")) {
            //create a new instance of Actor
            tempMovie = new Movie();
            tempMovie.setDirector(tempDirector);
        } else if (qName.equalsIgnoreCase("cats")) {
            //create a new instance of Actor
            tempGenres = new HashSet<>();
            tempMovie.setDirector(tempDirector);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempString = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("Film")) {
            //add it to the list
            movies.add(tempMovie);

        } else if (qName.equalsIgnoreCase("dirname")) {
            tempDirector = tempString;
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovie.setTitle(tempString);
        } else if (qName.equalsIgnoreCase("year")) {
            tempMovie.setYear(tempString);
        } else if (qName.equalsIgnoreCase("cat")) {
            tempGenres.add(tempString);
        } else if (qName.equalsIgnoreCase("cats")) {
            tempMovie.setGenres(tempGenres);
        }
    }

    public static void main(String[] args) {
        MoviesParser mp = new MoviesParser();
        mp.run();
    }
}