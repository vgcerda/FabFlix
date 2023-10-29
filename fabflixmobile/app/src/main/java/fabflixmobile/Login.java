package fabflixmobile;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

public class Login extends ActionBarActivity {

    private EditText username;
    private EditText password;
    private TextView message;
    private Button loginButton;

    /*
      In Android, localhost is the address of the device or the emulator.
      To connect to your machine, you need to use the below IP address
     */
    //    private final String host = "10.0.2.2";
    private final String host = "18.216.38.168";
    private final String port = "8443";
    //    private final String domain = "cs122b-spring21-team-50-war";
    private final String domain = "cs122b-spring21-team-50";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // upon creation, inflate and initialize the layout
        setContentView(R.layout.login);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        message = findViewById(R.id.message);
        loginButton = findViewById(R.id.login);

        //assign a listener to call a function to handle the user request when clicking a button
        loginButton.setOnClickListener(view -> login());
    }

    public void login() {

        message.setText("Trying to login");
        // use the same network queue across our application
        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest loginRequest = new StringRequest(
                Request.Method.POST,
                baseURL + "/api/login",
                response -> {
                    JSONObject obj = null;
                    String status = "";
                    try {
                        obj = new JSONObject(response);
                        status = obj.getString("status");
                        if (status.equals("success")) {
                            Log.d("login.success", response);
                            // initialize the activity(page)/destination
                            Intent searchPage = new Intent(Login.this, SearchViewActivity.class);
                            // activate the list page.
                            startActivity(searchPage);
                        }
                        else {
                            message.setText("Invalid Credentials");
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // error
                    Log.d("login.error", error.toString());
                }) {
            @Override
            protected Map<String, String> getParams() {
                // POST request form data
                final Map<String, String> params = new HashMap<>();
                params.put("username", username.getText().toString());
                params.put("password", password.getText().toString());
                params.put("type", "android");

                return params;
            }
        };

        // important: queue.add is where the login request is actually sent
        queue.add(loginRequest);

    }
}