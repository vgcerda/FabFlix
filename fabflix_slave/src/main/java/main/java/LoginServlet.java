package main.java;
import com.google.gson.JsonObject;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.jasypt.util.password.StrongPasswordEncryptor;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet{

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/read");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String type = request.getParameter("type");
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");


        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Verify reCAPTCHA
        if (type == null) {
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
        }

        try{
            Connection dbcon = dataSource.getConnection();


            String query = "SELECT * FROM customers c WHERE c.email = ?;";

            // Declare our statement
            PreparedStatement statement = dbcon.prepareStatement(query);
            statement.setString(1, username);

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
                    // set this user into the session
                    request.getSession().setAttribute("user", new User(username, rs.getString("id"), "customer"));

                    responseJsonObject.addProperty("status", "success");
                    responseJsonObject.addProperty("message", "success");
                } else {
                    // Login fail
                    responseJsonObject.addProperty("status", "fail");

                    // sample error messages. in practice, it is not a good idea to tell user which one is incorrect/not exist.
                    //                if (!username.equals("anteater")) {
                    //                    responseJsonObject.addProperty("message", "user " + username + " doesn't exist");
                    //                } else {
                    //                    responseJsonObject.addProperty("message", "incorrect password");
                    //                }
                    responseJsonObject.addProperty("message", "Login Fail: Wrong Credentials");
                }
            } else {
                responseJsonObject.addProperty("status", "fail");
                responseJsonObject.addProperty("message", "Login Fail: Wrong Credentials");
            }
            out.write(responseJsonObject.toString());

            response.setStatus(200);

            rs.close();
            statement.close();
//            connection.close();
            dbcon.close();

        } catch (Exception e){

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
