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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "DashboardServlet", urlPatterns = "/_dashboard")

public class DashboardServlet extends HttpServlet{
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
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
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");

        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            jsonObject.addProperty("status", "fail");
            jsonObject.addProperty("message", "Login Fail: Missing Recaptcha");
            out.write(jsonObject.toString());

            response.setStatus(200);
//            // set response status to 500 (Internal Server Error)
//            response.setStatus(500);
//
//            System.out.println("Response <LOGIN SERVLET>: " + jsonObject.toString());

            out.close();
            return;
        }

        try{
            Connection dbcon = dataSource.getConnection();


            String query = "SELECT * FROM employees e WHERE e.email = ?;";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);
            statement.setString(1, email);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
            JsonObject responseJsonObject = new JsonObject();

            if(rs.next()) {
                boolean success = false;
                success = new StrongPasswordEncryptor().checkPassword(password, rs.getString("password"));

                if (success) {
                    // Login success:
                    request.getSession().setAttribute("user", new User(email, rs.getString("fullname"), "admin"));

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    // Login fail
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Login Fail: Wrong Credentials");
                }
            }
            out.write(responseJsonObject.toString());

            response.setStatus(200);

            rs.close();
            statement.close();
            dbcon.close();

        } catch (Exception e){

            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // set response status to 500 (Internal Server Error)
            response.setStatus(500);
        }
        out.close();
    }

}