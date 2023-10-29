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

@WebServlet(name = "SingleStarServlet", urlPatterns = "/api/single-star")
public class SingleStarServlet extends HttpServlet{
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

        // change this to your own mysql username and password
//        String loginUser = "mytestuser";
//        String loginPasswd = "Mytest!123";
//        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";
        HttpSession session = request.getSession(true);

        response.setContentType("application/json");

        String id = request.getParameter("id");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {
//            Class.forName("com.mysql.jdbc.Driver").newInstance();
//
//            // create database connection
//            Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
//
//            // declare statement
//            Statement statement = connection.createStatement();
//
//            // prepare query
//            String query = new StringBuilder().append("SELECT id, name, IFNULL(birthYear, 'N/A') birthYear, (SELECT GROUP_CONCAT(m.title, '/', m.id)\n").append("\tFROM stars_in_movies sm, movies m, stars s1\n").append("    WHERE s1.id = s.id AND m.id = sm.movieId AND sm.starId = s1.id\n").append("    ) movies\n").append("FROM stars s\n").append("WHERE s.id = ").append("'" + id + "'").append(";\n").toString();
//
////            String query = "SELECT m.id, m.title from ratings r, movies m where r.movieId == m.id order by ratings limit 20";
//            // execute query
//            ResultSet rs = statement.executeQuery(query);

            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            // Declare our statement
//            Statement statement = dbcon.createStatement();

            String query = "SELECT id, name, IFNULL(birthYear, 'N/A') birthYear, (SELECT GROUP_CONCAT(m.title, '/', m.id ORDER BY (SELECT m1.year\n" +
                    "\tFROM movies m1\n" +
                    "    WHERE m1.id = m.id) DESC, m.title ASC)\n" +
                    "\tFROM stars_in_movies sm, movies m, stars s1\n" +
                    "    WHERE s1.id = s.id AND m.id = sm.movieId AND sm.starId = s1.id\n) movies\n" +
                    "\tFROM stars s\n" + "WHERE s.id = ?;";

            PreparedStatement statement = dbcon.prepareStatement(query);

            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject respData = new JsonObject();
            respData.addProperty("prevURL", (String) session.getAttribute("prevURL"));
            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
//                String movie_id = rs.getString("id");
                String single_star_name = rs.getString("name");
                String single_star_year = rs.getString("birthYear");
                String single_star_movies = rs.getString("movies");

                JsonArray tempArray = new JsonArray();
                String[] t = single_star_movies.split(",");
                for (int j = 0; j < t.length; j++) {
                    tempArray.add(t[j]);
                }

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("single_star_name", single_star_name);
                jsonObject.addProperty("single_star_year", single_star_year);
                jsonObject.add("single_star_movies", tempArray);

                jsonArray.add(jsonObject);
            }
            respData.add("data", jsonArray);
            // write JSON string to output
            out.write(respData.toString());
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
