package main.java;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "Payments", urlPatterns = "/api/payments")
public class Payments extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        HttpSession session = request.getSession();

        PrintWriter out = response.getWriter();

        JsonObject sales = (JsonObject) session.getAttribute("sales");
        if(sales != null){
            out.write(sales.toString());
            response.setStatus(200);
        }else{
            response.setStatus(500);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        String fname = request.getParameter("fname");
        String lname = request.getParameter("lname");
        String card_num = request.getParameter("card-num");
        String exp = request.getParameter("exp");


        JsonObject responseJsonObject = new JsonObject();

        HashMap<String, JsonObject> shoppingCart = (HashMap<String, JsonObject>) session.getAttribute("shoppingCart");

        PrintWriter out = response.getWriter();

        try{

            Connection dbcon = dataSource.getConnection();

            // Declare our statement


            String queryCredit = "SELECT EXISTS(SELECT * FROM creditcards WHERE ? = id AND ? = expiration) AS matched;\n";
            PreparedStatement statement1 = dbcon.prepareStatement(queryCredit);
            statement1.setString(1, card_num);
            statement1.setString(2, exp);

            ResultSet rs = statement1.executeQuery();



            rs.next();
            if(rs.getBoolean("matched")){

                Date d = new Date(System.currentTimeMillis());
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

                String formattedDate = df.format(d);

                User u = (User)session.getAttribute("user");
                int userId = Integer.parseInt(u.getUserId());

//                String queryCustomerId = "SELECT customerId FROM CUSTOMERS WHERE '" + u.getUsername() + "' = email";

//                ResultSet rs2 = statement.executeQuery(queryCustomerId);

                //insert into sales table

                String queryInsertSale = "INSERT INTO sales(customerId, movieId, saleDate)\n" +
                        "VALUES \n";

                int i = 0;
                int len = shoppingCart.size() - 1;
//                for(Map.Entry<String, JsonObject> entry : shoppingCart.entrySet()) {
//                    String key = entry.getKey();
//                    JsonObject obj = entry.getValue();
//
//                    int qty = obj.get("quantity").getAsInt();
//
//                    for(int j = 0; j < qty; j++){
//
//                        if(i == len && j == (qty - 1)){
//                            queryInsertSale += "(" + u.getUserId() + ", '" + key + "', '" + formattedDate + "');";
////                            System.out.println("(" + u.getUserId() + ", '" + key + "', '" + formattedDate + "');");
//                        }else{
//                            queryInsertSale += "(" + u.getUserId() + ", '" + key + "', '" + formattedDate + "'),\n";
////                            System.out.println("(" + u.getUserId() + ", '" + key + "', '" + formattedDate + "'),\n");
//                        }
//                    }
//                    i++;
//                }
                JsonObject sales = new JsonObject();
                String saleIdQuery = "select id from sales ORDER BY id DESC limit 1;";
                Statement statementSaleId = dbcon.createStatement();
                ResultSet rsSaleId = statementSaleId.executeQuery(saleIdQuery);
                int saleId = 0;
                if(rsSaleId.next()){
                    saleId = rsSaleId.getInt("id");
                }

                for(Map.Entry<String, JsonObject> entry : shoppingCart.entrySet()) {
                    JsonObject obj = entry.getValue();

                    int qty = obj.get("quantity").getAsInt();
                    saleId++;
                    sales.add(Integer.toString(saleId), obj);
                    saleId += qty - 1;
                    for(int j = 0; j < qty; j++){

                        if(i == len && j == (qty - 1)){
                            queryInsertSale += "(?, ?, ?);";
//                            System.out.println("(" + u.getUserId() + ", '" + key + "', '" + formattedDate + "');");
                        }else{
                            queryInsertSale += "(?, ?, ?),\n";
//                            System.out.println("(" + u.getUserId() + ", '" + key + "', '" + formattedDate + "'),\n");
                        }
                    }
                    i++;
                }

                session.setAttribute("sales", sales);

                i = 1;
                PreparedStatement statement2 = dbcon.prepareStatement(queryInsertSale);
                for(Map.Entry<String, JsonObject> entry : shoppingCart.entrySet()) {
                    String key = entry.getKey();
                    JsonObject obj = entry.getValue();

                    int qty = obj.get("quantity").getAsInt();

                    for(int j = 0; j < qty; j++) {
                        statement2.setInt(i, userId);
                        statement2.setString(i + 1, key);
                        statement2.setString(i + 2, formattedDate);
                        i += 3;
                    }
                }

                statement2.executeUpdate();

                statement2.close();
                response.setStatus(200);
            }else{
                response.setStatus(500);
            }

            rs.close();
            statement1.close();

            dbcon.close();

        }catch(Exception e){

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
