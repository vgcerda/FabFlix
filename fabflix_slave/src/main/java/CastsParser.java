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


public class CastsParser extends DefaultHandler {
    private static FileWriter file;
    private String errors;
    private int duplicates;
    private int starsInserted;
    private int unidentifiedActor;
    private int inserted;
    private int skipped;

    List<MovieCast> movieCasts;

    HashMap<String, String> starsFromStarsTable;
    HashMap<String, String> moviesFromMoviesTable;
    HashSet<String> starsInMovies;


    private long startTime;

    private String tempString;
    private MovieCast tempMovieCast;
    private HashSet<String> tempCast;
    private String tempDirector;

    private int starIdReference;

    private Connection dbcon;

    public CastsParser() {
        startTime = System.nanoTime();
        errors = "";
        duplicates = 0;
        starsInserted = 0;
        unidentifiedActor = 0;
        inserted = 0;
        skipped = 0;

        starsFromStarsTable = new HashMap<>();
        moviesFromMoviesTable = new HashMap<>();
        starsInMovies = new HashSet<>();
        movieCasts = new ArrayList<>();

        try {
            String jdbcURL="jdbc:mysql://localhost:3306/moviedb";
            dbcon = DriverManager.getConnection(jdbcURL, "mytestuser", "Team!!50");

            Statement s = dbcon.createStatement();

            ResultSet lastIdResult = s.executeQuery("SELECT MAX(id) last_id FROM stars;");
            while (lastIdResult.next()) {
                starIdReference = Integer.parseInt(lastIdResult.getString("last_id").substring(2));
            }
            lastIdResult.close();

            ResultSet starsResult = s.executeQuery("SELECT id, name FROM stars");

            while (starsResult.next()) {
                String id = starsResult.getString("id");
                String name = starsResult.getString("name").toLowerCase();
                starsFromStarsTable.put(name, id);
            }
            starsResult.close();

            ResultSet moviesResult = s.executeQuery("SELECT id, title, director FROM movies");

            String temp = "";
            while (moviesResult.next()) {
                String id = moviesResult.getString("id");
                String title = moviesResult.getString("title").toLowerCase();
                String director = moviesResult.getString("director").toLowerCase();
                temp = title + "/" + director;
                moviesFromMoviesTable.put(temp, id);
            }
            moviesResult.close();

            ResultSet starsInMoviesResult = s.executeQuery("SELECT starId, movieId FROM stars_in_movies");

            while (starsInMoviesResult.next()) {
                String starId = starsInMoviesResult.getString("starId");
                String movieId = starsInMoviesResult.getString("movieId");
                temp = starId + "/" + movieId;
                starsInMovies.add(temp);
            }
            starsInMoviesResult.close();

            s.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {

        parseDocument();
        insertMovieCasts();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("Parsing Casts XML took: " + duration + "ms.");
        System.out.println("Duplicates Found: " + duplicates);
        System.out.println("Cast Members Inserted Into stars_in_movies: " + inserted);
        System.out.println("New Stars Inserted Into stars: " + starsInserted);
        System.out.println("Unidentified Stars Found: " + unidentifiedActor);
        System.out.println("Entries Skipped: " + skipped);

    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("/home/ubuntu/casts124.xml", this);

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
    private void insertMovieCasts() {
        try {
            file = new FileWriter("casts124_inconsistencies.txt");

            dbcon.setAutoCommit(false);

            String insertActorQuery = "INSERT INTO stars values (?, ?, ?)";
            String insertActorInMovieQuery = "INSERT INTO stars_in_movies values (?, ?)";
            PreparedStatement psInsertActor = dbcon.prepareStatement(insertActorQuery);
            PreparedStatement psInsertActorInMovie = dbcon.prepareStatement(insertActorInMovieQuery);

            Iterator<MovieCast> it = movieCasts.iterator();
            while (it.hasNext()) {
                MovieCast mc = it.next();
                String movieTitle = mc.getMovieTitle().trim().toLowerCase();
                String director = mc.getDirector().trim().toLowerCase();
                HashSet<String> cast = mc.getCast();

                String movieCheck = movieTitle + "/" + director;
                if (moviesFromMoviesTable.containsKey(movieCheck)) {
                    String movieId = moviesFromMoviesTable.get(movieCheck);
                    Iterator<String> castIt = cast.iterator();
                    while (castIt.hasNext()) {
                        String star = castIt.next().trim();
                        String starId = "";

                        if (star.equalsIgnoreCase("sa") || star.equalsIgnoreCase("s a")) {
                            unidentifiedActor++;
                            System.out.println("Skipped Unidentified Actor");
                        }
                        else {
                            if (starsFromStarsTable.containsKey(star.toLowerCase())) {
                                starId = starsFromStarsTable.get(star.toLowerCase());
                            }
                            else {
                                starIdReference++;
                                starsInserted++;
                                if (Integer.toString(starIdReference).length() < 7) {
                                    starId = "nm0" + starIdReference;
                                } else {
                                    starId = "nm" + starIdReference;
                                }
                                psInsertActor.setString(1, starId);
                                psInsertActor.setString(2, star);
                                psInsertActor.setNull(3, Types.NULL);
                                psInsertActor.addBatch();
                                starsFromStarsTable.put(star.toLowerCase(), starId);
                            }
                            String starInMovieCheck = starId + "/" + movieId;
                            if (!starsInMovies.contains(starInMovieCheck)) {
                                inserted++;
                                psInsertActorInMovie.setString(1, starId);
                                psInsertActorInMovie.setString(2, movieId);
                                psInsertActorInMovie.addBatch();
                                starsInMovies.add(starInMovieCheck);
                            } else {
                                duplicates++;
                                System.out.println(String.format("Entry with star name '%s' in movie '%s' is a duplicate insert entry", star, movieTitle));
                                errors += String.format("Entry with star name '%s' in movie '%s' is a duplicate insert entry", star, movieTitle) + "\n";
                            }
                        }
                    }
                } else {
                    skipped++;
                    System.out.println(String.format("Entry with movie title '%s' not found in database", movieTitle));
                    errors += String.format("Entry with movie title '%s' not found in database", movieTitle) + "\n";
                }
            }
            file.write(errors);
            psInsertActor.executeBatch();
            psInsertActorInMovie.executeBatch();
            dbcon.commit();
            dbcon.close();
            psInsertActor.close();
            psInsertActorInMovie.close();
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

//    private void naiveInsertActors() {
//        try {
////            dbcon.setAutoCommit(false);
//
//            String insertActorQuery = "INSERT INTO stars values (?, ?, ?)";
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
        if (qName.equalsIgnoreCase("dirfilms")) {
            tempDirector = "";
        } else if (qName.equalsIgnoreCase("filmc")) {
            tempMovieCast = new MovieCast();
            tempCast = new HashSet<>();
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempString = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("filmc")) {
            tempMovieCast.setCast(tempCast);
            tempMovieCast.setDirector(tempDirector);
            movieCasts.add(tempMovieCast);
        } else if (qName.equalsIgnoreCase("is")) {
            tempDirector = tempString;
        } else if (qName.equalsIgnoreCase("a")) {
            tempCast.add(tempString);
        } else if (qName.equalsIgnoreCase("t")) {
            tempMovieCast.setMovieTitle(tempString);
        }

    }

    public static void main(String[] args) {
        CastsParser cp = new CastsParser();
        cp.run();
    }
}