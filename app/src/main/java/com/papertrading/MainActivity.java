package com.papertrading;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private LinearLayout stockLayout;
    private EditText searchBar;


    private List<Stock> filteredStocks = new ArrayList<>(); //new
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_LAST_DOWNLOAD_DATE = "lastDownloadDate";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the UI
        initUI();
        dbHelper = new DatabaseHelper(this);
        showWatchlist();

        Button watchlistButton = findViewById(R.id.btn_watchlist);
        watchlistButton.setOnClickListener(view -> {
            showWatchlist();
        });
        Button ordersButton = findViewById(R.id.btn_orders);
        ordersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, OrdersActivity.class);
                startActivity(intent);
            }
        });


        if (!dbHelper.isDownloadedToday()) {
            // If not, execute the download task
            InstrumentsUpdate instrumentsUpdate = new InstrumentsUpdate(dbHelper);
            instrumentsUpdate.execute();
            // Set the last download date to today
            dbHelper.setDownloadedToday();
            Log.d("CSV downloaded","downloaded");
        }
        else{
            Log.d("Not downloaded","Not downloaded");
        }


    }

    private void initUI() {
        // Get reference to the LinearLayout where stocks will be displayed
        stockLayout = findViewById(R.id.stock_layout);

        // Get reference to the search bar
        searchBar = findViewById(R.id.search_bar);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // Filter the stocks based on the search query
                filterStocks(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}


        });

        // Initialize the lists
        dbHelper = new DatabaseHelper(this);
        //allStocks = dbHelper.getAllStocks();
        //filteredStocks = new ArrayList<>(allStocks);

        // Display the initial message
        TextView noStocksTextView = new TextView(this);
        noStocksTextView.setText("No stocks selected");
        noStocksTextView.setTextSize(16);
        noStocksTextView.setPadding(16, 8, 16, 8);
        stockLayout.addView(noStocksTextView);

    }
    private void filterStocks(String query) {
        Log.d("FilterStocks", "Query: " + query); // Log the search query
        // Fetch filtered stocks from the database
        filteredStocks = dbHelper.searchStocks(query);

        // Log the filtered stocks
        for (Stock stock : filteredStocks) {
            Log.d("FilterStocks", "Filtered Stock: " + stock.getTradingSymbol());
        }

        // Update the UI with the filtered list of stock
        updateUI(stockLayout, filteredStocks);
    }
    private void updateUI(LinearLayout layout, List<Stock> stocks) {
        layout.removeAllViews(); // Clear the layout before adding new views

        // Check if there are stocks to display
        if (stocks.isEmpty()) {
            // Display a message indicating no stocks found
            TextView noStocksTextView = new TextView(this);
            noStocksTextView.setText("No stocks found");
            noStocksTextView.setTextSize(16);
            noStocksTextView.setPadding(16, 8, 16, 8);
            layout.addView(noStocksTextView);
        } else {
            // Add views for each stock and divider
            for (Stock stock : stocks) {
                TextView textView = new TextView(this);
                textView.setText(stock.getTradingSymbol());

                // Add an OnClickListener to each stock TextView (existing code)
                textView.setOnClickListener(view -> {
                    // Add the clicked stock to the watchlist
                    addStockToWatchlist(stock);

                    // Display a toast message to indicate that the stock was added to the watchlist
                    Toast.makeText(MainActivity.this, "Added to watchlist: " + stock.getTradingSymbol(), Toast.LENGTH_SHORT).show();

                    // Update the UI to reflect the change
                    updateUI(layout, filteredStocks); // Update using the provided layout
                });

                // Add the TextView for the stock to the layout
                textView.setTextSize(16);
                textView.setPadding(16, 24, 16, 24);
                layout.addView(textView);

                // Create and add the divider View
                View divider = new View(this);
                divider.setBackgroundColor(Color.GRAY);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 5)); // Set height to 5dp
                layout.addView(divider);


            }
        }
    }



    protected void showWatchlist() {
        // Fetch watchlisted stocks from the database
        List<Stock> watchlistStocks = dbHelper.getWatchlistStocks();

        // Update the UI to display the watchlisted stocks
        updateWatchlistUI(watchlistStocks);
    }

    private void updateWatchlistUI(List<Stock> watchlistStocks) {
        stockLayout.removeAllViews(); // Clear existing views

        if (!watchlistStocks.isEmpty()) {
            // Add views for each watchlisted stock
            for (Stock stock : watchlistStocks) {
                TextView textView = new TextView(this);
                textView.setText(stock.getTradingSymbol());
                // Customize text view properties as needed

                textView.setTextSize(16);
                textView.setPadding(16, 24, 16, 24);

                // Set click listener to start BuySellActivity when a watchlisted stock is clicked
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Start BuySellActivity
                        Intent intent = new Intent(MainActivity.this, BuySellActivity.class);
                        intent.putExtra("Trading_Symbol", stock.getTradingSymbol());
                        startActivity(intent);
                    }
                });

                // Add the TextView to the layout
                stockLayout.addView(textView);

                // Create and add the divider View
                View divider = new View(this);
                divider.setBackgroundColor(Color.GRAY);
                divider.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 5)); // Set height to 5dp
                stockLayout.addView(divider);
            }
        } else {
            // If watchlist is empty, display a message
            TextView noWatchlistTextView = new TextView(this);
            noWatchlistTextView.setText("No stocks in watchlist");
            // Customize text view properties as needed
            stockLayout.addView(noWatchlistTextView);
        }
    }

    private void addStockToWatchlist(Stock stock) {
        // Add the stock to the watchlist table in the database
        dbHelper.addToWatchlist(stock.getTradingSymbol());
    }

}