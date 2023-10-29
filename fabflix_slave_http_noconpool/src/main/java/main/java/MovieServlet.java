package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//import javax.annotation.Resource;
//import javax.servlet.ServletException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;



@WebServlet(name = "MovieServlet", urlPatterns = "/api/movies")
public class MovieServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;

//    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;


    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/read");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private String buildSearchQuery(HttpServletRequest request) {
        String title = request.getParameter("title");
        String director = request.getParameter("director");
        String year = request.getParameter("year");
        String star_name = request.getParameter("star_name");
        String query = "SELECT m.id, m.title, m.year, m.director, (SELECT GROUP_CONCAT(limited_genres.name ORDER BY limited_genres.name ASC)\n" +
                "        FROM (SELECT g.name\n" +
                "\t\t\tFROM movies m1, genres g, genres_in_movies gim\n" +
                "            WHERE m1.id = m.id AND gim.genreId = g.id AND m1.id = gim.movieId\n" +
                "            LIMIT 3) limited_genres) genres, \n" +
                "\t\t(SELECT GROUP_CONCAT(limited_stars.name, '/', limited_stars.id ORDER BY (SELECT COUNT(*)\n" +
                "\t\tFROM stars_in_movies\n" +
                "        WHERE limited_stars.id = stars_in_movies.starId) DESC, limited_stars.name ASC)\n" +
                "\t\tFROM (SELECT s.name, s.id\n" +
                "\t\t\tFROM movies m2, stars s, stars_in_movies sim \n" +
                "\t\t\tWHERE m2.id = m.id AND sim.starId = s.id AND m2.id = sim.movieId\n" +
                "\t\t\tLIMIT 3) limited_stars) stars,\n" +
                "\t\t(SELECT IFNULL(r.rating, 'N/A') FROM ratings r WHERE r.movieId = m.id) rating\n" +
                "FROM movies m\n" +
                "WHERE 1";

        if (title != null && !title.equals("")) {
//            query += " AND title LIKE ?\n";
            query += " AND MATCH (m.title) AGAINST (?IN BOOLEAN MODE)\n";
        }
        if (director != null && !director.equals("")) {
            query += " AND director LIKE ?\n";
        }
        if (year != null && !year.equals("")) {
            query += " AND year = ?\n";
        }
        if (star_name != null && !star_name.equals("")) {
            query += " AND EXISTS(SELECT name\n" +
                    "\t\tFROM stars s1, stars_in_movies sim1 \n" +
                    "        WHERE s1.id = sim1.starId AND movieId = m.id AND s1.name LIKE ?)\n";
        }
        return query;
    }

    private String buildBrowseQuery(HttpServletRequest request) {
        String browseBy = request.getParameter("by");
        String query = "SELECT m.id, m.title, m.year, m.director, (SELECT GROUP_CONCAT(limited_genres.name ORDER BY limited_genres.name ASC)\n" +
                "        FROM (SELECT g.name\n" +
                "\t\t\tFROM movies m1, genres g, genres_in_movies gim\n" +
                "            WHERE m1.id = m.id AND gim.genreId = g.id AND m1.id = gim.movieId\n" +
                "            LIMIT 3) limited_genres) genres, \n" +
                "\t\t(SELECT GROUP_CONCAT(limited_stars.name, '/', limited_stars.id ORDER BY (SELECT COUNT(*)\n" +
                "\t\tFROM stars_in_movies\n" +
                "        WHERE limited_stars.id = stars_in_movies.starId) DESC, limited_stars.name ASC)\n" +
                "\t\tFROM (SELECT s.name, s.id\n" +
                "\t\t\tFROM movies m2, stars s, stars_in_movies sim \n" +
                "\t\t\tWHERE m2.id = m.id AND sim.starId = s.id AND m2.id = sim.movieId\n" +
                "\t\t\tLIMIT 3) limited_stars) stars,\n" +
                "\t\t(SELECT IFNULL(r.rating, 'N/A') FROM ratings r WHERE r.movieId = m.id) rating\n" +
                "FROM movies m\n" +
                "WHERE 1";

        if (browseBy.equals("genre")) {
            query += " AND ? in (SELECT name\n" +
                    "\t\tFROM genres g1, genres_in_movies gim1\n" +
                    "        WHERE g1.id = gim1.genreId AND movieId = m.id)";
        }
        else {
            String titlestart = request.getParameter("titlestart");
            if (titlestart.equals("*")) {
                query += " AND m.title REGEXP '^[^a-zA-Z0-9].*$'\n";
            }
            else {
                query += " AND m.title LIKE ?\n";
            }
        }
        return query;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            // /home/ubuntu/logs
            String contextPath = "/home/ubuntu/logs";
            String txtFilePath = contextPath + "/slaveLogs.txt";
            File myfile = new File(txtFilePath);
            myfile.createNewFile();

            PrintWriter pw = new PrintWriter(new FileOutputStream(myfile, true));

            long startTimeTS = System.nanoTime();

            HttpSession session = request.getSession(true);
            session.setAttribute("prevURL", '?' + request.getQueryString());

            response.setContentType("application/json");

            // Output stream to STDOUT
            PrintWriter out = response.getWriter();

            try {
                // Get a connection from dataSource
                Connection dbcon = dataSource.getConnection();

//            String query = new StringBuilder().append("SELECT m.id, m.title, m.year, m.director, (SELECT GROUP_CONCAT(limited_genres.name)\n").append("        FROM (SELECT g.name\n").append("\t\t\tFROM movies m1, genres g, genres_in_movies gim\n").append("            WHERE m1.id = m.id AND gim.genreId = g.id AND m1.id = gim.movieId\n").append("            LIMIT 3) limited_genres) genres, \n").append("\t\t(SELECT GROUP_CONCAT(limited_stars.name, '/', limited_stars.id)\n").append("\t\tFROM (SELECT s.name, s.id\n").append("\t\t\tFROM movies m2, stars s, stars_in_movies sim \n").append("\t\t\tWHERE m2.id = m.id AND sim.starId = s.id AND m2.id = sim.movieId\n").append("\t\t\tLIMIT 3) limited_stars) stars,\n").append("\t\tr.rating\n").append("FROM ratings r, movies m\n").append("WHERE r.movieId = m.id\n").append("ORDER BY r.rating DESC\n").append("LIMIT 20;\n").toString();
                String query = "";

                if (request.getParameter("type").equals("search")) {
                    query = buildSearchQuery(request);
                } else if (request.getParameter("type").equals("browse")) {
                    query = buildBrowseQuery(request);
                }

                // Put into function l8r
                String sortByParam = request.getParameter("sortBy");
                if (!sortByParam.equals("none")) {
                    String[] sortFilters = sortByParam.split("-");
                    if (sortFilters[0].equals("title")) {
                        query += "ORDER BY m.title ";
                        if (sortFilters[1].equals("asc"))
                            query += "ASC";
                        else {
                            query += "DESC";
                        }
                        query += ", rating ";
                        if (sortFilters[3].equals("asc")) {
                            query += "ASC";
                        } else {
                            query += "DESC";
                        }
                    }
                    if (sortFilters[0].equals("rating")) {
                        query += "ORDER BY rating ";
                        if (sortFilters[1].equals("asc"))
                            query += "ASC";
                        else {
                            query += "DESC";
                        }
                        query += ", m.title ";
                        if (sortFilters[3].equals("asc")) {
                            query += "ASC";
                        } else {
                            query += "DESC";
                        }
                    }
                }


                int pageParam = Integer.parseInt(request.getParameter("page"));
                int n = Integer.parseInt(request.getParameter("nElements"));
                int offset = (pageParam - 1) * n;

                query += " LIMIT ? OFFSET ?;";

                // Declare our statement
                PreparedStatement statement = dbcon.prepareStatement(query);

                int i = 1;
                if (request.getParameter("type").equals("search")) {
                    String title = request.getParameter("title");
                    String director = request.getParameter("director");
                    String year = request.getParameter("year");
                    String star_name = request.getParameter("star_name");

                    if (title != null && !title.equals("")) {
                        String[] temp1 = title.split(" ");
                        String temp2 = "";
                        for (int j = 0; j < temp1.length; j++) {
                            temp2 += "+" + temp1[j] + "* ";
//                        temp2 += temp1[j] + "* ";
                        }
//                    statement.setString(i, "%" + title + "%");
                        statement.setString(i, temp2);
                        i++;
                    }
                    if (director != null && !director.equals("")) {
                        statement.setString(i, "%" + director + "%");
                        i++;
                    }
                    if (year != null && !year.equals("")) {
                        statement.setInt(i, Integer.parseInt(year));
                        i++;
                    }
                    if (star_name != null && !star_name.equals("")) {
                        statement.setString(i, "%" + star_name + "%");
                        i++;
                    }
                } else if (request.getParameter("type").equals("browse")) {
                    String browseBy = request.getParameter("by");

                    if (browseBy.equals("genre")) {
                        String genre = request.getParameter("genre");
                        statement.setString(i, genre);
                        i++;
                    } else {
                        String titlestart = request.getParameter("titlestart");
                        if (!titlestart.equals("*")) {
                            statement.setString(i, titlestart + "%");
                            i++;
                        }
                    }
                }

                statement.setInt(i, n + 1);
                statement.setInt(i + 1, offset);

                long startTimeTJ = System.nanoTime();
                // Perform the query
                ResultSet rs = statement.executeQuery();

                long endTimeTJ = System.nanoTime();
                long elapsedTimeTJ = endTimeTJ - startTimeTJ;
                pw.print(elapsedTimeTJ + " ");

                JsonArray jsonArray = new JsonArray();

                // Iterate through each row of rs
//            String prev_movie_id = "";
                int x = 0;
                while (x < n && rs.next()) {
                    JsonObject jsonObject = new JsonObject();

                    String movie_id = rs.getString("id");
                    String movie_title = rs.getString("title");
                    String movie_year = rs.getString("year");
                    String movie_director = rs.getString("director");
                    String movie_genres = rs.getString("genres");
                    String movie_stars = rs.getString("stars");

                    if (movie_genres == null) {
                        jsonObject.addProperty("movie_genres", "N/A");
                    } else {
                        String[] t = movie_genres.split(",");

                        JsonArray genresArray = new JsonArray();

                        for (int j = 0; j < t.length; j++) {
                            genresArray.add(t[j]);
                        }
                        jsonObject.add("movie_genres", genresArray);
                    }

                    if (movie_stars == null) {
                        jsonObject.addProperty("movie_stars", "N/A");
                    } else {
                        String[] t = movie_stars.split(",");

                        JsonArray starsArray = new JsonArray();

                        for (int j = 0; j < t.length; j++) {
                            starsArray.add(t[j]);
                        }
                        jsonObject.add("movie_stars", starsArray);
                    }


//                String movie_stars_id = rs.getString("stars_id");
                    String movie_ratings = rs.getString("rating");

                    // Create a JsonObject based on the data we retrieve from rs

                    jsonObject.addProperty("movie_id", movie_id);
                    jsonObject.addProperty("movie_title", movie_title);
                    jsonObject.addProperty("movie_year", movie_year);
                    jsonObject.addProperty("movie_director", movie_director);
                    jsonObject.addProperty("movie_ratings", movie_ratings);

                    jsonArray.add(jsonObject);
                    x++;
                }
                JsonObject keepObject = new JsonObject();
                if (x < n || !(rs.next())) {
                    keepObject.addProperty("keep", 0);
                } else {
                    keepObject.addProperty("keep", 1);
                }
                jsonArray.add(keepObject);

                // write JSON string to output
                out.write(jsonArray.toString());
                // set response status to 200 (OK)
                response.setStatus(200);

                rs.close();
                statement.close();
//            connection.close();
                dbcon.close();

            } catch (Exception e) {

                // write error message JSON object to output
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("errorMessage", e.getMessage());
                out.write(jsonObject.toString());

                // set response status to 500 (Internal Server Error)
                response.setStatus(500);

            }
            out.close();

            long endTimeTS = System.nanoTime();
            long elapsedTimeTS = endTimeTS - startTimeTS;
            pw.println(elapsedTimeTS);
            pw.close();
        } catch (Exception e) {
            response.setStatus(500);
        }
    }
}
