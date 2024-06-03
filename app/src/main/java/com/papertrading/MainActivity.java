package com.papertrading;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity implements RabbitMQConnection.MessageListener{
    private DatabaseHelper dbHelper;
    protected LinearLayout stockLayout;
    private EditText searchBar;
    private RabbitMQConnection rbmqconnect;




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
        String id = generateOrRetrieveUUID();
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
        Button pnlButton = findViewById(R.id.btn_pnl);
        pnlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PnLActivity.class);
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






        rbmqconnect = new RabbitMQConnection(dbHelper,stockLayout);
        rbmqconnect.setMessageListener(this);

        // Call startConsuming after rbmqconnect is initialized
        rbmqconnect.startConsuming();

    }
    @Override
    public void onPriceUpdateReceived(long instrumentToken, double price) {
        // Update the watchlist UI with the received price update
        updateWatchlistUI(instrumentToken, price);



    }

    private void updateWatchlistUI(long instrumentToken, double price) {
        // Find the corresponding TextView in the watchlist UI based on instrument token
        // Update the TextView with the new price
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        String tradingSymbol = dbHelper.getTradingSymbolByInstrumentToken(instrumentToken);

        if (tradingSymbol != null) {
            for (int i = 0; i < stockLayout.getChildCount(); i++) {
                View view = stockLayout.getChildAt(i);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    String text = textView.getText().toString();
                    if (text.contains(tradingSymbol)) {
                        // Update the price dynamically
                        textView.setText(tradingSymbol + " - ₹" + price);
                        break;
                    }
                }
            }
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
                textView.setText(stock.getTradingSymbol() + " - ₹" + stock.getPrice());
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
        List<Long> instrumentTokens = new ArrayList<>();
        List<Stock> watchlistStocks = dbHelper.getWatchlistStocks();
        for (Stock watch : watchlistStocks) {
            instrumentTokens.add(dbHelper.getInstrumentTokenByTradingSymbol(watch.getTradingSymbol()));
        }


        String id = generateOrRetrieveUUID();


        callApiForStock(instrumentTokens, id);
    }


    private void callApiForStock(List<Long> instrument_token, String id) {

        AsyncTask.execute(() -> {
            try {


                String instrumentTokenString = instrument_token.stream()
                        .map(Object::toString)
                        .collect(Collectors.joining(","));
                URL url = new URL("http://192.168.1.4:8282/api/watchlist"+"?id="+id+"&instrumentToken="+instrumentTokenString.toString());
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    Log.d("API Response", result.toString());
                } finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e("API Error", "Error making API call", e);
            }
        });
    }


    private String generateOrRetrieveUUID() {
        String uuid = null;

            // Check if UUID exists in the database
            if (dbHelper.hasUniqueId()) {
                // Retrieve the UUID from the database
                uuid = dbHelper.retrieveUniqueId();
            } else {
                // Generate and store a new UUID in the database
                uuid = dbHelper.generateAndStoreUniqueId();
            }


        return uuid;
    }


}