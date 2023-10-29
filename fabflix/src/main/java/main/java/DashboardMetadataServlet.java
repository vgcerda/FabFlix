package main.java;
import com.google.gson.JsonArray;
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


@WebServlet(name = "DashboardMetadataServlet", urlPatterns = "/_dashboard/metadata")
public class DashboardMetadataServlet extends HttpServlet{

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json");

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        try{
            Connection dbcon = dataSource.getConnection();
            DatabaseMetaData metaData = dbcon.getMetaData();
            ResultSet rsTables = metaData.getTables(null, null, null, new String[]{"TABLE"});

            JsonArray jsonArray = new JsonArray();

            while(rsTables.next()) {
                JsonObject jsonObject = new JsonObject();
                String systemTableName = rsTables.getString("TABLE_NAME");
                jsonObject.addProperty("table_name", systemTableName);

                ResultSet rsColumns = metaData.getColumns(null,null, systemTableName, null);

                JsonArray columnsArray = new JsonArray();
                while(rsColumns.next()){
                    JsonObject colObject = new JsonObject();
                    String columnName = rsColumns.getString("COLUMN_NAME");
                    String datatype = rsColumns.getString("TYPE_NAME");
                    colObject.addProperty("column_name", columnName);
                    colObject.addProperty("datatype", datatype);

                    columnsArray.add(colObject);
                }
                jsonObject.add("column_array", columnsArray);
                jsonArray.add(jsonObject);

                rsColumns.close();
            }

            // write JSON string to output
            out.write(jsonArray.toString());
            response.setStatus(200);
            rsTables.close();
            dbcon.close();

        }catch(Exception e){
            e.printStackTrace();
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
