package com.papertrading;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import android.widget.LinearLayout;


import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private List<Stock> allStocks;
    private List<Stock> filteredStocks;
    private List<Stock> watchlistStocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the UI
        initUI();
        dbHelper = new DatabaseHelper(this);

        Button watchlistButton = findViewById(R.id.btn_watchlist);
        watchlistButton.setOnClickListener(view -> showWatchlist());

        // Download and parse CSV data
        //downloadAndParseCSV("https://www.algogreek.com/instruments.csv");
    }

    private LinearLayout stockLayout;
    private EditText searchBar;



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
        allStocks = dbHelper.getAllStocks();
        filteredStocks = new ArrayList<>(allStocks);

        // Display the initial message
        TextView noStocksTextView = new TextView(this);
        noStocksTextView.setText("No stocks selected");
        noStocksTextView.setTextSize(16);
        noStocksTextView.setPadding(16, 8, 16, 8);
        stockLayout.addView(noStocksTextView);

    }

    private void filterStocks(String query) {
        // Filter the list of stocks based on the search query
        filteredStocks.clear();
        for (Stock stock : allStocks) {
            if (stock.getTradingSymbol().toLowerCase().contains(query.toLowerCase())) {
                filteredStocks.add(stock);
            }
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
            // Add views for each stock
            for (Stock stock : stocks) {
                TextView textView = new TextView(this);
                textView.setText(stock.getTradingSymbol() + ": " + stock.getLastPrice());

                // Add an OnClickListener to each stock TextView
                textView.setOnClickListener(view -> {
                    // Add the clicked stock to the watchlist
                    stock.setInWatchlist(true);

                    // Update the database to reflect the change
                    dbHelper.updateStockInWatchlist(stock);

                    // Display a toast message to indicate that the stock was added to the watchlist
                    Toast.makeText(MainActivity.this, "Added to watchlist: " + stock.getTradingSymbol(), Toast.LENGTH_SHORT).show();

                    // Update the UI to reflect the change
                    updateUI(layout, stocks); // Update using the provided layout
                });

                // Add the TextView for the stock to the layout
                textView.setTextSize(16);
                textView.setPadding(16, 8, 16, 8);
                layout.addView(textView);
            }
        }
    }

    private void showWatchlist() {
        // Fetch watchlisted stocks from the database
        watchlistStocks = dbHelper.getWatchlistStocks();

        // Update the UI to display the watchlisted stocks
        updateWatchlistUI(watchlistStocks);
    }

    private void updateWatchlistUI(List<Stock> watchlistStocks) {
        stockLayout.removeAllViews(); // Clear existing views

        if (!watchlistStocks.isEmpty()) {
            // Add views for each watchlisted stock
            for (Stock stock : watchlistStocks) {
                TextView textView = new TextView(this);
                textView.setText(stock.getTradingSymbol() + ": " + stock.getLastPrice());
                // Customize text view properties as needed
                stockLayout.addView(textView);
            }
        } else {
            // If watchlist is empty, display a message
            TextView noWatchlistTextView = new TextView(this);
            noWatchlistTextView.setText("No stocks in watchlist");
            // Customize text view properties as needed
            stockLayout.addView(noWatchlistTextView);
        }
    }



    public void downloadAndParseCSV(String urlString) {
        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] columns = line.split(",");
                    // Skip headers or add condition to ignore malformed lines
                    if (columns.length == 12) {
                        // Call insertStockIntoDatabase using dbHelper instance
                        dbHelper.insertStockIntoDatabase(columns);

                    }
                }
                Log.d("CSV_DOWNLOAD", "Inserted all stocks " );

                reader.close();
                inputStream.close();
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    // yooooo
}