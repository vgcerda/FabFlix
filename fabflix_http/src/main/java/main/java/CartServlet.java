package main.java;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * This IndexServlet is declared in the web annotation below,
 * which is mapped to the URL pattern /api/index.
 */
@WebServlet(name = "CartServlet", urlPatterns = "/api/cart")
public class CartServlet extends HttpServlet {

    /**
     * handles GET requests to store session information
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();

        JsonObject responseJsonObject = new JsonObject();

        HashMap<String, JsonObject> shoppingCart = (HashMap<String, JsonObject>) session.getAttribute("shoppingCart");
        if (shoppingCart == null) {
            shoppingCart = new HashMap<String, JsonObject>();
        }
        JsonObject shoppingCartJsonObject = new JsonObject();
        shoppingCart.forEach(shoppingCartJsonObject::add);
        responseJsonObject.add("shoppingCart", shoppingCartJsonObject);

        // write all the data into the jsonObject
        response.getWriter().write(responseJsonObject.toString());
    }

    /**
     * handles POST requests to add and show the item list information
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String itemId = request.getParameter("itemId");
        String title = request.getParameter("title");
        String action = request.getParameter("action");

        HttpSession session = request.getSession();
        HashMap<String, JsonObject> shoppingCart = (HashMap<String, JsonObject>) session.getAttribute("shoppingCart");

        int doAct = 0;

        if(action.equals("minus")){
            doAct = -1;
        } else if (action.equals("plus")){
            doAct = 1;
        } else {
            doAct = -1 * shoppingCart.get(itemId).get("quantity").getAsInt();
        }

        // get the previous items in a ArrayList

        JsonObject itemObj = new JsonObject();

        if (shoppingCart == null) {
            itemObj.addProperty("title", title);
            itemObj.addProperty("price", 10);
            itemObj.addProperty("quantity", 1);
            shoppingCart = new HashMap<String, JsonObject>();
            shoppingCart.put(itemId, itemObj);
            session.setAttribute("shoppingCart", shoppingCart);
        } else {
            if(!shoppingCart.containsKey(itemId)){
                itemObj.addProperty("title", title);
                itemObj.addProperty("price", 10);
                itemObj.addProperty("quantity", 1);

                // prevent corrupted states through sharing under multi-threads
                // will only be executed by one thread at a time
                synchronized (shoppingCart) {
                    shoppingCart.put(itemId, itemObj);
                }
            }else{
                synchronized (shoppingCart){
                    int prevQty = shoppingCart.get(itemId).get("quantity").getAsInt() + doAct;
                    if(prevQty == 0){
                        shoppingCart.remove(itemId);
                    }else {
                        shoppingCart.get(itemId).addProperty("quantity", prevQty);
                    }
                }
            }
        }

        System.out.println(shoppingCart.toString());

//        JsonObject responseJsonObject = new JsonObject();
//
//        JsonArray shoppingCartJsonArray = new JsonArray();
//        shoppingCart.forEach(shoppingCartJsonArray::add);
//        responseJsonObject.add("shoppingCart", shoppingCartJsonArray);
//
//        response.getWriter().write(responseJsonObject.toString());
        response.setStatus(200);
    }
}