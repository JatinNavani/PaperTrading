package com.papertrading;



import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BuySellActivity extends MainActivity {

    private DatabaseHelper dbHelper;
    private double buyPrice;
    private double sellPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_sell);

        dbHelper = new DatabaseHelper(this);

        String tradingSymbol = getIntent().getStringExtra("Trading_Symbol");

        Button buyButton = findViewById(R.id.buy_button);
        Button sellButton = findViewById(R.id.sell_button);
        TextView stockNameTextView = findViewById(R.id.trading_symbol_text_view);
        EditText quantityEditText = findViewById(R.id.quantity_edit_text);
        stockNameTextView.setText(tradingSymbol);

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get quantity input
                String quantity = quantityEditText.getText().toString();
                // Store data in orders table (replace this with your database logic)
                String orderId = UUID.randomUUID().toString();
                long instrument_token =dbHelper.getInstrumentTokenByTradingSymbol(tradingSymbol);
                storeOrder(tradingSymbol, "buy", quantity,0.0,instrument_token,orderId);
                Toast.makeText(BuySellActivity.this, "Buy Order Placed: " + tradingSymbol + ", Quantity: " + quantity, Toast.LENGTH_SHORT).show();
                callApiForOrder(instrument_token,"buy",orderId);
            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get quantity input
                String quantity = quantityEditText.getText().toString();
                // Store data in orders table (replace this with your database logic)
                String orderId = UUID.randomUUID().toString();
                long instrument_token =dbHelper.getInstrumentTokenByTradingSymbol(tradingSymbol);
                storeOrder(tradingSymbol, "sell", quantity,0.0,instrument_token,orderId);
                Toast.makeText(BuySellActivity.this, "Sell Order Placed: " + tradingSymbol + ", Quantity: " + quantity, Toast.LENGTH_SHORT).show();
                callApiForOrder(instrument_token,"sell",orderId);

            }
        });

        Button watchlistButton = findViewById(R.id.btn_watchlist);
        watchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuySellActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

        Button ordersButton = findViewById(R.id.btn_orders);
        ordersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuySellActivity.this, OrdersActivity.class);
                startActivity(intent);
            }
        });

        Button pnlButton = findViewById(R.id.btn_pnl);
        pnlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BuySellActivity.this, PnLActivity.class);
                startActivity(intent);
            }
        });
    }
    private void storeOrder(String tradingSymbol, String orderType, String quantity,double quotePrice,long instrument_token,String orderId) {
        // Get a writable database instance
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Stock stock = dbHelper.getStockByTradingSymbol(tradingSymbol);

        // Create a ContentValues object to hold the values to be inserted
        ContentValues values = new ContentValues();
        // Assuming you have default values for other columns like price, instrument_token, name, etc.
        values.put("id", orderId);
        values.put("price", quotePrice); // Default price
        values.put("type", orderType);
        values.put("instrument_token", instrument_token); // Default instrument token
        values.put("name", ""); // Default name
        values.put("exchange_token", 0); // Default exchange token
        values.put("tradingsymbol", tradingSymbol);
        values.put("exchange", ""); // Default exchange
        values.put("quantity", Integer.parseInt(quantity));// Convert quantity to integer
        values.put("status", "Pending");

        // Insert the values into the orders table
        long newRowId = db.insert("orders", null, values);

        // Check if the insertion was successful
        if (newRowId != -1) {
            // Insertion successful
            Toast.makeText(BuySellActivity.this, "Order placed: " + orderType + " " + quantity + " of " + tradingSymbol, Toast.LENGTH_SHORT).show();
        } else {
            // Insertion failed
            Toast.makeText(BuySellActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
        }

        // Close the database connection
        db.close();
    }
    private void callApiForOrder(Long instrument_token,String direction,String orderId) {

        AsyncTask.execute(() -> {
            try {

                URL url = new URL("http://192.168.1.4:8282/getQuote"+"?instrumentToken="+instrument_token);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    InputStream in = urlConnection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    buyPrice = parseBuyPriceFromApiResponse(result.toString());
                    sellPrice = parseSellPriceFromApiResponse(result.toString());
                    Log.d("API Response", result.toString());
                } finally {
                    urlConnection.disconnect();
                    if(direction == "buy"){
                        updateOrder(orderId, buyPrice,"Executed");
                    }else{
                        updateOrder(orderId, sellPrice,"Executed");
                    }

                }
            } catch (IOException e) {
                Log.e("API Error", "Error making API call", e);
                if(direction == "buy"){
                    updateOrder(orderId, buyPrice,"Failed");
                }else{
                    updateOrder(orderId, sellPrice,"Failed");
                }
            }
        });
    }

    private double parseBuyPriceFromApiResponse(String apiResponse) {
        // Parse the API response to extract the buy price
        // Assuming the API response is in JSON format and contains a field named "buyPrice"
        try {
            JSONObject jsonResponse = new JSONObject(apiResponse);
            return jsonResponse.getDouble("buyPrice");
        } catch (JSONException e) {
            Log.e("API Error", "Error parsing buy price from API response", e);
            return 0.0; // Return a default value in case of an error
        }
    }

    private double parseSellPriceFromApiResponse(String apiResponse) {
        // Parse the API response to extract the sell price
        // Assuming the API response is in JSON format and contains a field named "sellPrice"
        try {
            JSONObject jsonResponse = new JSONObject(apiResponse);
            return jsonResponse.getDouble("sellPrice");
        } catch (JSONException e) {
            Log.e("API Error", "Error parsing sell price from API response", e);
            return 0.0; // Return a default value in case of an error
        }
    }

    private void updateOrder(String orderId, double price, String status) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("price", price);// Assuming "price" is the column you want to update
        values.put("status", status);
        // Define the WHERE clause to specify which row to update (using orderId)
        String whereClause = "id = ?";
        String[] whereArgs = { orderId };

        // Execute the update operation
        int rowsAffected = db.update("orders", values, whereClause, whereArgs);

        // Check if the update was successful
        if (rowsAffected > 0) {
            // Update successful
            Log.d("Update Order", "Order with ID " + orderId + " updated successfully");
        } else {
            // Update failed
            Log.e("Update Order", "Failed to update order with ID " + orderId);
        }

        // Close the database connection
        db.close();
    }

}
