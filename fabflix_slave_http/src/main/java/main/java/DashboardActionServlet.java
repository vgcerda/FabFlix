package main.java;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet(name = "DashboardActionServlet", urlPatterns = "/_dashboard/action")

public class DashboardActionServlet extends HttpServlet{
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/write");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException{

        try {
            RequestDispatcher rd = request.getRequestDispatcher("/dashboard-login.html");
            rd.forward(request, response);


        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException{
        String type = request.getParameter("type");
        System.out.println("type: "+ type);
        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try{
            Connection dbcon = dataSource.getConnection();

            String query = "";

            JsonObject obj = new JsonObject();
            if(type.equals("star")){
                System.out.println("Inside Star");
                //TODO: get_next_id Stored Function
                String queryStarId = "SELECT get_next_id('stars') AS newStarId";
                Statement statementId = dbcon.createStatement();
                ResultSet newId = statementId.executeQuery(queryStarId);
                System.out.println("Inside Star      1");
                newId.next();
                String newStarId = newId.getString("newStarId");
                query = "INSERT INTO stars VALUES (?, ?, ?);";
                System.out.println("Inside Star      2");
                String name = request.getParameter("name");
                int birthYear = 0;

                boolean birthYearExist = false;
                if(request.getParameter("birthYear") != null && !request.getParameter("birthYear").isEmpty()){
                    System.out.println("Inside Star      3");
                    birthYearExist = true;
                    try {
                        System.out.println("Inside Star      4");
                        birthYear = Integer.parseInt(request.getParameter("birthYear"));
                    }catch(Exception e){
                        System.out.println("Inside Star      5");
                        e.printStackTrace();
                        birthYearExist = false;
                    }
                }
                System.out.println("Inside Star      6");
                System.out.println("pre-prepared statement");
                //declare statement
                PreparedStatement statement = dbcon.prepareStatement(query);
                statement.setString(1, newStarId);
                statement.setString(2, name);
                System.out.println("Inside Star      7");
                if(birthYearExist){
                    System.out.println("Inside Star      8");
                    statement.setInt(3, birthYear);
                    System.out.println("birthYear exist");
                }else{
                    System.out.println("Inside Star      9");
                    statement.setNull(3, Types.NULL);
                    System.out.println("birthYear doesn't exist");
                }

                // Perform the query
                System.out.println("Inside Star      10");
                int rs = statement.executeUpdate();
                System.out.println("Inside Star      11");
                statement.close();
                System.out.println("result int: "+ rs);
                System.out.println("Inside Star      12");
                if(rs > 0){
                    System.out.println("Inside Star      13");
                    obj.addProperty("starId", newStarId);
                    response.setStatus(200);
                }else{
                    System.out.println("Inside Star      14");
                    response.setStatus(500);
                }
//                rs.close();

            }else{
                System.out.println("Inside movie");
                String title = request.getParameter("title");
                int year = Integer.parseInt(request.getParameter("year"));
                String director = request.getParameter("director");
                String starName = request.getParameter("star_name");
                String genre = request.getParameter("genre");

                //check if movie doesnt exist
//                String movieExistQuery = "SELECT EXISTS (SELECT * FROM MOVIES WHERE title = ? AND year = ? AND director = ?) AS MOVIE_EXISTS";
//                PreparedStatement statement1 = dbcon.prepareStatement(movieExistQuery);
//                statement1.setString(1, title);
//                statement1.setInt(2, year);
//                statement1.setString(3, director);
//                ResultSet movieExistRs = statement1.executeQuery();

//                movieExistRs.next();
//                Integer exist = Integer.parseInt(movieExistRs.getString("MOVIE_EXISTS"));

                //TODO: add_movie Stored Function
                query = "CALL add_movie(?, ?, ?, ?, ?, @ret);";

                //declare statement
                PreparedStatement statement = dbcon.prepareStatement(query);
                statement.setString(1, title);
                statement.setInt(2, year);
                statement.setString(3, director);
                statement.setString(4, starName);
                statement.setString(5, genre);

                // Perform the query
                ResultSet rs = statement.executeQuery();
                statement.close();

                query = "SELECT @ret;";
                Statement statement2 = dbcon.createStatement();
                ResultSet rs2 = statement2.executeQuery(query);

                rs2.next();
                String result = rs2.getString("@ret");
                if(result.equals("")){
                    response.setStatus(500);
                }else{
                    String[] rsArr = result.split("/");
                    obj.addProperty("movieId", rsArr[0]);
                    obj.addProperty("starId", rsArr[1]);
                    obj.addProperty("genreId", rsArr[2]);
                    response.setStatus(200);
                }

                rs.close();
                rs2.close();
                statement2.close();
            }

            /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
//            JsonObject responseJsonObject = new JsonObject();
//
//            out.write(responseJsonObject.toString());
            out.write(obj.toString());

            dbcon.close();

        } catch (Exception e){

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            System.out.println("hello from catch");
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }

}