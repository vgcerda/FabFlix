package fabflixmobile;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.content.Intent;


public class SearchViewActivity extends Activity {
    private EditText searchBar;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.searchview);
        searchBar = findViewById(R.id.search_bar);
        searchButton = findViewById(R.id.search_button);

        //assign a listener to call a function to handle the user request when clicking a button
        searchButton.setOnClickListener(view -> initiateSearch());
        TextView.OnEditorActionListener keyListener = new TextView.OnEditorActionListener(){
            public boolean onEditorAction(TextView exampleView, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_NULL
                        && event.getAction() == KeyEvent.ACTION_DOWN) {
                    initiateSearch();
                }
                return true;
            }
        };
        searchBar.setOnEditorActionListener(keyListener);
    }

    public void initiateSearch() {
        String searchQuery = searchBar.getText().toString();
        Intent listViewActivity = new Intent(SearchViewActivity.this, ListViewActivity.class);
        listViewActivity.putExtra("query", searchQuery);
        startActivity(listViewActivity);
    }
}