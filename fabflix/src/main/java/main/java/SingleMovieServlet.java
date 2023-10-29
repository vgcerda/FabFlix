package main.java;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

//import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
//import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet{
    private static final long serialVersionUID = 1L;

//    @Resource(name = "jdbc/moviedb")
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);

        response.setContentType("application/json");

        String id = request.getParameter("id");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            String query = "SELECT m.id, m.title, m.year, m.director, (SELECT IFNULL(rating, 'N/A')\n" + "\tFROM ratings r\n" + "\tWHERE r.movieId = m.id) rating,\n" + "\t(SELECT GROUP_CONCAT(g.name ORDER BY g.name)\n" + "\tFROM movies m1, genres g, genres_in_movies gim\n" + "\tWHERE m1.id = m.id AND gim.genreId = g.id AND m1.id = gim.movieId) genres,\n" + "\t(SELECT GROUP_CONCAT(s.name, '/', s.id ORDER BY (SELECT COUNT(*)\n" +
                    "\t\tFROM stars_in_movies\n" +
                    "        WHERE s.id = stars_in_movies.starId) DESC, s.name ASC)\n" +
                    "\tFROM movies m2, stars s, stars_in_movies sim\n" + "\tWHERE m2.id = m.id AND sim.starId = s.id AND m2.id = sim.movieId) stars\n" + "FROM movies m\n" + "WHERE m.id = ?;\n";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject respData = new JsonObject();
            respData.addProperty("prevURL", (String) session.getAttribute("prevURL"));
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                JsonObject jsonObject = new JsonObject();

                String single_movie_id = rs.getString("id");
                String single_movie_title = rs.getString("title");
                String single_movie_year = rs.getString("year");
                String single_movie_director = rs.getString("director");
                String single_movie_genres = rs.getString("genres");
                String single_movie_stars = rs.getString("stars");

                if (single_movie_genres == null) {
                    jsonObject.addProperty("single_movie_genres", "N/A");
                }
                else {
                    String[] t = single_movie_genres.split(",");

                    JsonArray genresArray = new JsonArray();

                    for (int j = 0; j < t.length; j++) {
                        genresArray.add(t[j]);
                    }
                    jsonObject.add("single_movie_genres", genresArray);
                }

                if (single_movie_stars == null) {
                    jsonObject.addProperty("single_movie_stars", "N/A");
                }
                else {
                    String[] t = single_movie_stars.split(",");

                    JsonArray starsArray = new JsonArray();

                    for (int j = 0; j < t.length; j++) {
                        starsArray.add(t[j]);
                    }
                    jsonObject.add("single_movie_stars", starsArray);
                }

                String single_movie_rating = rs.getString("rating");

                jsonObject.addProperty("single_movie_id", single_movie_id);
                jsonObject.addProperty("single_movie_title", single_movie_title);
                jsonObject.addProperty("single_movie_year", single_movie_year);
                jsonObject.addProperty("single_movie_director", single_movie_director);
                jsonObject.addProperty("single_movie_rating", single_movie_rating);

                jsonArray.add(jsonObject);
            }

            respData.add("data", jsonArray);
            // write JSON string to output
            out.write(respData.toString());
//            out.write(jsonArray.toString());
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

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);

        }
        out.close();
    }
}
