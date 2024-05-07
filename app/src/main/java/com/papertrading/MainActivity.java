package com.papertrading;

import androidx.appcompat.app.AppCompatActivity;

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

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private List<Stock> allStocks;
    private List<Stock> filteredStocks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the UI
        initUI();

        // Download and parse CSV data
        //downloadAndParseCSV("https://www.algogreek.com/instruments.csv");
    }

    private LinearLayout stockLayout;

    private void initUI() {
        // Get reference to the LinearLayout where stocks will be displayed
        LinearLayout stockLayout = findViewById(R.id.stock_layout);

        // Initialize the search bar
        EditText searchBar = findViewById(R.id.search_bar);
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

        // Display the initial list of stocks
        updateUI(stockLayout, filteredStocks);
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
        LinearLayout stockLayout = findViewById(R.id.stock_layout);
        updateUI(stockLayout, filteredStocks);
    }


    private void updateUI(LinearLayout layout, List<Stock> stocks) {
        layout.removeAllViews(); // Clear the layout before adding new views
        for (Stock stock : stocks) {
            TextView textView = new TextView(this);
            textView.setText(stock.getTradingSymbol() + ": " + stock.getLastPrice());

            // Check if the stock is already in the watchlist
            if (!stock.isInWatchlist()) {
                // Add the "+" button if the stock is not in the watchlist
                Button addButton = new Button(this);
                addButton.setText("+");
                addButton.setOnClickListener(view -> {
                    // Add the stock to the watchlist
                    stock.setInWatchlist(true);

                    // Update the database to reflect the change
                    dbHelper.updateStockInWatchlist(stock);

                    // Update the UI to reflect the change
                    updateUI(layout, stocks); // Update using the provided layout
                });

                // Add the button to the layout
                LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                buttonParams.setMargins(10, 0, 0, 0); // Adjust margins as needed
                addButton.setLayoutParams(buttonParams);
                layout.addView(addButton);
            }

            // Add the TextView for the stock
            textView.setTextSize(16);
            textView.setPadding(16, 8, 16, 8);
            layout.addView(textView);
        }

    }

/*
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
    } */
    // yooooo
}