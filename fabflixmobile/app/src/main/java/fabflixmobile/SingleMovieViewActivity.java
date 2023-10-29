package fabflixmobile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class SingleMovieViewActivity extends Activity {
    //    private final String host = "10.0.2.2";
    private final String host = "18.216.38.168";
    private final String port = "8443";
    //    private final String domain = "cs122b-spring21-team-50-war";
    private final String domain = "cs122b-spring21-team-50";
    private final String baseURL = "https://" + host + ":" + port + "/" + domain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.singlemovieview);

        System.out.println("Helo");

        Intent intent = getIntent();
        String id = intent.getStringExtra("id");

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;
        // request type is POST
        final StringRequest singleMovieRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/single-movie?id=" + id,
                response -> {
                    JSONObject obj1 = null;
                    try {
                        obj1 = new JSONObject(response);
                        JSONArray arr = obj1.getJSONArray("data");
                        JSONObject obj2 = arr.getJSONObject(0);

                        String title = obj2.getString("single_movie_title");
                        String year = obj2.getString("single_movie_year");
                        String director = obj2.getString("single_movie_director");
                        JSONArray genres = obj2.getJSONArray("single_movie_genres");
                        JSONArray stars = obj2.getJSONArray("single_movie_stars");
                        String tempGenres = "";

                        for (int j = 0; j < genres.length(); j++) {
                            tempGenres += genres.getString(j);
                            if (j != genres.length() - 1) {
                                tempGenres += ", ";
                            }
                        }
                        String tempStars = "";
                        for (int j = 0; j < stars.length(); j++) {
                            tempStars += stars.getString(j).split("/")[0];
                            if (j != stars.length() - 1) {
                                tempStars += ", ";
                            }
                        }

                        TextView titleView = findViewById(R.id.title);
                        TextView subtitleView = findViewById(R.id.subtitle);
                        TextView directorView = findViewById(R.id.director);
                        TextView genresView = findViewById(R.id.genres);
                        TextView starsView = findViewById(R.id.stars);

                        titleView.setText(title);
                        // need to cast the year to a string to set the label
                        subtitleView.setText(year);
                        directorView.setText(director);
                        genresView.setText(tempGenres);
                        starsView.setText(tempStars);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // error
                    Log.d("singleMovie.error", error.toString());
                });
        queue.add(singleMovieRequest);
    }
}