import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.sql.DataSource;

import com.google.gson.JsonObject;
import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

public class UpdateSecurePasswordEmployees {

    //    private static DataSource dataSource;
//
//    public void init(ServletConfig config) {
//        try {
//            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
//        } catch (NamingException e) {
//            e.printStackTrace();
//        }
//    }
    /*
     *
     * This program updates your existing moviedb employees table to change the
     * plain text passwords to encrypted passwords.
     *
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     *
     */
    public static void main(String[] args) throws Exception {


        try {
//            Connection dbcon = dataSource.getConnection();
//
            String loginUser = "mytestuser";
            String loginPasswd = "Team!!50";
            String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

            Class.forName("com.mysql.jdbc.Driver").newInstance();
            Connection dbcon = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
            Statement statement = dbcon.createStatement();

            // change the employees table password column from VARCHAR(20) to VARCHAR(128)
            String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
            int alterResult = statement.executeUpdate(alterQuery);
            System.out.println("altering employees table schema completed, " + alterResult + " rows affected");

            // get the ID and password for each customer
            String query = "SELECT email, password from employees";

            ResultSet rs = statement.executeQuery(query);

            // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
            //  it internally use SHA-256 algorithm and 10,000 iterations to calculate the encrypted password
            PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

            ArrayList<String> updateQueryList = new ArrayList<>();

            System.out.println("encrypting password (this might take a while)");
            while (rs.next()) {
                // get the ID and plain text password from current table
                String email = rs.getString("email");
                String password = rs.getString("password");

                // encrypt the password using StrongPasswordEncryptor
                String encryptedPassword = passwordEncryptor.encryptPassword(password);

                // generate the update query
                String updateQuery = String.format("UPDATE employees SET password='%s' WHERE email='%s';", encryptedPassword,
                        email);
                updateQueryList.add(updateQuery);
            }
            rs.close();

            // execute the update queries to update the password
            System.out.println("updating password");
            int count = 0;
            for (String updateQuery : updateQueryList) {
                int updateResult = statement.executeUpdate(updateQuery);
                count += updateResult;
            }
            System.out.println("updating password completed, " + count + " rows affected");

            statement.close();
//            connection.close();
            dbcon.close();

            System.out.println("finished");
        }catch(Exception e){
            // write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            System.out.println(jsonObject.toString());

            // set reponse status to 500 (Internal Server Error)
//            response.setStatus(500);
        }

    }
}

