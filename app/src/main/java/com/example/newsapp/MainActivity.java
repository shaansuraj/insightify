package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.newsapp.Models.NewsApiResponse;
import com.example.newsapp.Models.NewsHeadlines;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SelectListener, View.OnClickListener {

    RecyclerView recyclerView;
    CustomAdapter adapter;
    Button b1, b2, b3, b4, b5, b6, b7;
    SearchView searchView;
    String category = "general";  // Default category
    ShimmerFrameLayout shimmerFrameLayout;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set navigation bar color (for devices with Lollipop or higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(getResources().getColor(R.color.black));
        }

        // Firebase authentication check
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), login.class);
            startActivity(intent);
            finish();
        }

        // Initialize UI elements
        searchView = findViewById(R.id.search_view);
        recyclerView = findViewById(R.id.recycler_main);
        shimmerFrameLayout = findViewById(R.id.shimmer);
        shimmerFrameLayout.startShimmer();

        // SearchView listener for querying news
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Fetch news based on the search query and category
                RequestManager manager = new RequestManager(MainActivity.this);
                manager.getNewsHeadlines(listener, category, query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Button listeners for category selection
        b1 = findViewById(R.id.btn_1);
        b1.setOnClickListener(this);
        b2 = findViewById(R.id.btn_2);
        b2.setOnClickListener(this);
        b3 = findViewById(R.id.btn_3);
        b3.setOnClickListener(this);
        b4 = findViewById(R.id.btn_4);
        b4.setOnClickListener(this);
        b5 = findViewById(R.id.btn_5);
        b5.setOnClickListener(this);
        b6 = findViewById(R.id.btn_6);
        b6.setOnClickListener(this);
        b7 = findViewById(R.id.btn_7);
        b7.setOnClickListener(this);

        // Default category fetch
        RequestManager manager = new RequestManager(this);
        manager.getNewsHeadlines(listener, category, null);
    }

    // Listener for fetching data
    private final OnFetchDataListener<NewsApiResponse> listener = new OnFetchDataListener<NewsApiResponse>() {
        @Override
        public void onFetchData(List<NewsHeadlines> list, String message) {
            if (list.isEmpty()) {
                Toast.makeText(MainActivity.this, "No data found", Toast.LENGTH_SHORT).show();
            } else {
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE); // Hide shimmer
                recyclerView.setVisibility(View.VISIBLE);    // Show RecyclerView
                showNews(list); // Display news
            }
        }

        @Override
        public void onError(String message) {
            Toast.makeText(MainActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_LONG).show();
        }
    };

    // Function to display news in RecyclerView
    private void showNews(List<NewsHeadlines> list) {
        // Limit the number of items to prevent performance issues
        int maxItemsToDisplay = 50;
        List<NewsHeadlines> limitedList = list.size() > maxItemsToDisplay ? list.subList(0, maxItemsToDisplay) : list;

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        adapter = new CustomAdapter(this, limitedList, this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void OnNewsClicked(NewsHeadlines headlines) {
        // Open detailed view when a news item is clicked
        startActivity(new Intent(MainActivity.this, detailsView.class).putExtra("data", headlines));
    }

    @Override
    public void onClick(View v) {
        Button button = (Button) v;
        category = button.getText().toString();
        searchView.setQueryHint(category); // Update search hint
        RequestManager manager = new RequestManager(this);
        manager.getNewsHeadlines(listener, category, null); // Fetch news based on selected category
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about_us:
                startActivity(new Intent(MainActivity.this, AboutUs.class));
                break;

            case R.id.sign_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), login.class);
                startActivity(intent);
                finish();
                break;

            case R.id.summarizer:
                startActivity(new Intent(MainActivity.this, Summarizer.class));
                break;

            case R.id.favourite:
                startActivity(new Intent(MainActivity.this, Favourite.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
