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
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "BrowseServlet", urlPatterns = "/api/browse")
public class BrowseServlet extends HttpServlet{
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

        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try {

            // Get a connection from dataSource
            Connection dbcon = dataSource.getConnection();

            String query = "SELECT name\n" +
                    "FROM genres\n" +
                    "ORDER BY name";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);
            
            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
//            String prev_movie_id = "";
            while (rs.next()) {
                String genre = rs.getString("name");

                JsonObject jsonObject = new JsonObject();

                jsonObject.addProperty("genre", genre);

                jsonArray.add(jsonObject);
            }

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

            // set reponse status to 500 (Internal Server Error)
            response.setStatus(500);

        }
        out.close();
    }
}
