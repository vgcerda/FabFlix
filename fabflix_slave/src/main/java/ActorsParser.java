import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.xml.sax.helpers.DefaultHandler;

import java.sql.*;
//import javax.sql.DataSource;

public class ActorsParser extends DefaultHandler {
    private static FileWriter file;
    private String errors;
    private int duplicates;
    private int inserted;

    List<Actor> actors;

    HashSet<String> starsFromStarsTable;

    private long startTime;

    private String tempString;
    private Actor tempActor;
    private int actorIdReference;

    private Connection dbcon;
//    private DataSource dataSource;

    public ActorsParser() {
        startTime = System.nanoTime();
        errors = "";
        duplicates = 0;
        inserted = 0;

        starsFromStarsTable = new HashSet<>();
        actors = new ArrayList<>();

        try {
            String jdbcURL="jdbc:mysql://localhost:3306/moviedb";
            dbcon = DriverManager.getConnection(jdbcURL, "mytestuser", "Team!!50");

            Statement s = dbcon.createStatement();

            ResultSet lastIdResult = s.executeQuery("SELECT MAX(id) last_id FROM stars;");
            while (lastIdResult.next()) {
                actorIdReference = Integer.parseInt(lastIdResult.getString("last_id").substring(2));
            }
            lastIdResult.close();

            ResultSet starsResult = s.executeQuery("SELECT name, birthYear FROM stars");
//            Actor a;

            String temp = "";
            while (starsResult.next()) {
//                a = new Actor();
//                a.setName(starsResult.getString("name"));
                temp = starsResult.getString("name") + "/";
//                if (temp.equals("Marlon Brando/")){
//                    test.add(new Actor("Marlon Brando", starsResult.getString("birthYear")));
//                }
                String birthYear = starsResult.getString("birthYear");
                if (birthYear == null) {
//                    a.setBirthYear("");
                    temp += "";
                }
                else {
//                    a.setBirthYear(birthYear);
                    temp += birthYear;
                }
                starsFromStarsTable.add(temp);
            }

//            Actor marlon = new Actor("Marlon Brando", "1924");
//            String marlon = "Marlon Brando/1924";
//            for (Actor a: test) {
//                if (a.getName().equals("Marlon Brando")) {
//                    Actor marlon = new Actor("Marlon Brando", "1924");
//                    System.out.println(marlon.getName() + "/" + marlon.getBirthYear());
//                    System.out.println(a.getName() + "/" + a.getBirthYear());
//                }
//            }


            s.close();
            starsResult.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void run() {

        parseDocument();
        insertActors();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        System.out.println("Parsing Actors XML took: " + duration + "ms.");
        System.out.println("Duplicates Found: " + duplicates);
        System.out.println("Entries Inserted: " + inserted);
    }

    private void parseDocument() {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse("/home/ubuntu/actors63.xml", this);

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
    private void insertActors() {
        try {

            file = new FileWriter("actors63_inconsistencies.txt");

            dbcon.setAutoCommit(false);

            String insertActorQuery = "INSERT INTO stars values (?, ?, ?)";
            PreparedStatement ps = dbcon.prepareStatement(insertActorQuery);

            Iterator<Actor> it = actors.iterator();
            while (it.hasNext()) {
                Actor a = it.next();
                String name = a.getName().trim();
                String birthYear = a.getBirthYear();
                boolean hasBirthYear = true;
                try {
                    if (birthYear != null) {
                        birthYear = birthYear.trim();
                    }
                    Integer.parseInt(birthYear);
                } catch (NumberFormatException e) {
                    if (birthYear == null || !birthYear.equals("")) {
                        System.out.println(String.format("Actor entry with stagename '%s' has inconsistent dob format (dob = %s)", name, birthYear));
//                        errors.getAsJsonArray("birthYearFormattingError").add(String.format("Actor entry with stagename '%s' has inconsistent dob format (dob = %s)", name, birthYear));
                        errors += String.format("Actor entry with stagename '%s' has inconsistent dob format (dob = %s)", name, birthYear) + "\n";
                    }
                    hasBirthYear = false;
                    birthYear = "";
                }

                String check = name + "/" + birthYear;
                if (!starsFromStarsTable.contains(check)) {
                    inserted++;
                    actorIdReference++;
                    if (Integer.toString(actorIdReference).length() < 7) {
                        ps.setString(1, "nm0" + actorIdReference);
                    } else {
                        ps.setString(1, "nm" + actorIdReference);
                    }

                    ps.setString(2, name);
                    if (!hasBirthYear) {
                        ps.setNull(3, Types.NULL);
                    }
                    else{
                        ps.setInt(3, Integer.parseInt(birthYear));
                    }
                    ps.addBatch();
                    starsFromStarsTable.add(check);
                } else {
                    duplicates++;
                }
            }
            file.write(errors);
            ps.executeBatch();
            dbcon.commit();
            dbcon.close();
            ps.close();
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

    private void naiveInsertActors() {
        try {
//            dbcon.setAutoCommit(false);

            String insertActorQuery = "INSERT INTO stars values (?, ?, ?)";
            PreparedStatement ps = null;
            PreparedStatement psForExistsCheck = null;

            Iterator<Actor> it = actors.iterator();
            while (it.hasNext()) {
                Actor a = it.next();
                String name = a.getName();
                String birthYear = a.getBirthYear();
                boolean hasBirthYear = true;
                try {
                    Integer.parseInt(birthYear);
                } catch (NumberFormatException e) {
                    hasBirthYear = false;
                }

                String actorExistsQuery = "";

                if (hasBirthYear) {
                    actorExistsQuery = "SELECT EXISTS(SELECT * FROM stars WHERE name = ? AND birthYear = ?) actor_exists;";
                    psForExistsCheck = dbcon.prepareStatement(actorExistsQuery);
                    psForExistsCheck.setString(1, name);
                    psForExistsCheck.setInt(2, Integer.parseInt(birthYear));
                }
                else {
                    actorExistsQuery = "SELECT EXISTS(SELECT * FROM stars WHERE name = ? AND birthYear IS NULL) actor_exists;";
                    psForExistsCheck = dbcon.prepareStatement(actorExistsQuery);
                    psForExistsCheck.setString(1, name);
                }

                ResultSet actorExistsQueryResult = psForExistsCheck.executeQuery();

                while (actorExistsQueryResult.next()) {
                    boolean actorExists = actorExistsQueryResult.getBoolean("actor_exists");
                    if (!actorExists) {
                        actorIdReference++;
                        ps = dbcon.prepareStatement(insertActorQuery);
                        ps.setString(1, "nm" + actorIdReference);
                        ps.setString(2, name);
                        if (!hasBirthYear) {
                            ps.setNull(3, Types.NULL);
                        }
                        else{
                            ps.setInt(3, Integer.parseInt(birthYear));
                        }
                        ps.execute();
                    }
                }

            }

            dbcon.close();
            ps.close();
            psForExistsCheck.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Event Handlers
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //reset
        tempString = "";
        if (qName.equalsIgnoreCase("Actor")) {
            //create a new instance of Actor
            tempActor = new Actor();

//            tempActor.setType(attributes.getValue("type"));
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempString = new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.equalsIgnoreCase("Actor")) {
            //add it to the list
            actors.add(tempActor);

        } else if (qName.equalsIgnoreCase("stagename")) {
            tempActor.setName(tempString);
        } else if (qName.equalsIgnoreCase("dob")) {
            tempActor.setBirthYear(tempString);
        }

    }

    public static void main(String[] args) {
        ActorsParser ap = new ActorsParser();
        ap.run();
    }
}