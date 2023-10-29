package fabflixmobile;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import android.content.Intent;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListViewActivity extends Activity {
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

    private int page;

    private Button prevButton;
    private Button nextButton;

    private int keepNext;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.listview);

        page = 1;

        Intent intent = getIntent();
        query = intent.getStringExtra("query");

        prevButton = findViewById(R.id.previous);
        nextButton = findViewById(R.id.next);

        final RequestQueue queue = NetworkManager.sharedManager(this).queue;

        updateListView(queue);

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page--;
                updateListView(queue);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                page++;
                updateListView(queue);
            }
        });
    }

    public void checkButton() {
        if (page == 1) {
            prevButton.setEnabled(false);
        } else {
            prevButton.setEnabled(true);
        }
        if (keepNext == 0) {
            nextButton.setEnabled(false);
        } else {
            nextButton.setEnabled(true);
        }
    }

    public void updateListView(RequestQueue queue) {
        // request type is GET
        StringRequest searchRequest = new StringRequest(
                Request.Method.GET,
                baseURL + "/api/movies?title=" + query
                        + "&director="
                        + "&year="
                        + "&star_name="
                        + "&type=search"
                        + "&sortBy=none"
                        + "&nElements=20"
                        + "&page=" + page,
                response -> {
                    JSONArray arr;
                    try {
                        arr = new JSONArray(response);
                        String title = "";
                        String year = "";
                        String director = "";
                        JSONArray genres;
                        JSONArray stars;
                        String movie_id = "";
                        final ArrayList<Movie> movies = new ArrayList<>();
                        for (int i = 0; i < arr.length() - 1; i++) {
                            JSONObject obj = arr.getJSONObject(i);
                            title = obj.getString("movie_title");
                            year = obj.getString("movie_year");
                            director = obj.getString("movie_director");
                            genres = obj.getJSONArray("movie_genres");
                            stars = obj.getJSONArray("movie_stars");
                            movie_id = obj.getString("movie_id");

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
                            Movie m = new Movie(title, Short.valueOf(year), director, tempGenres, tempStars, movie_id);
                            movies.add(m);
                        }

                        keepNext = arr.getJSONObject(arr.length() - 1).getInt("keep");
                        MovieListViewAdapter adapter = new MovieListViewAdapter(movies, this);

                        ListView listView = findViewById(R.id.list);
                        listView.setAdapter(adapter);

                        listView.setOnItemClickListener((parent, view, position, id) -> {
                            Movie movie = movies.get(position);
                            String movieId = movie.getId();

                            Intent singleMoviePage = new Intent(ListViewActivity.this, SingleMovieViewActivity.class);
                            singleMoviePage.putExtra("id", movieId);
                            // activate the singleMoviePage page.
                            startActivity(singleMoviePage);
//                            String message = String.format("Clicked on position: %d, name: %s, %d, %s", position, movie.getName(), movie.getYear(), movie.getGenres());
//                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        });

                        checkButton();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // error
                    Log.d("ListViewActivity.error", error.toString());
                });
        queue.add(searchRequest);
    }
}