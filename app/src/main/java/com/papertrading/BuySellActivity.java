package com.papertrading;



import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BuySellActivity extends MainActivity {

    private DatabaseHelper dbHelper;

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
                storeOrder(tradingSymbol, "buy", quantity);
                // Display message
                Toast.makeText(BuySellActivity.this, "Buy Order Placed: " + tradingSymbol + ", Quantity: " + quantity, Toast.LENGTH_SHORT).show();
            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get quantity input
                String quantity = quantityEditText.getText().toString();
                // Store data in orders table (replace this with your database logic)
                storeOrder(tradingSymbol, "sell", quantity);
                // Display message
                Toast.makeText(BuySellActivity.this, "Sell Order Placed: " + tradingSymbol + ", Quantity: " + quantity, Toast.LENGTH_SHORT).show();
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
    }
    private void storeOrder(String tradingSymbol, String orderType, String quantity) {
        // Get a writable database instance
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Stock stock = dbHelper.getStockByTradingSymbol(tradingSymbol);

        // Create a ContentValues object to hold the values to be inserted
        ContentValues values = new ContentValues();
        // Assuming you have default values for other columns like price, instrument_token, name, etc.
        values.put("price", 0.0); // Default price
        values.put("type", orderType);
        values.put("instrument_token", 0); // Default instrument token
        values.put("name", ""); // Default name
        values.put("exchange_token", 0); // Default exchange token
        values.put("tradingsymbol", tradingSymbol);
        values.put("exchange", ""); // Default exchange
        values.put("quantity", Integer.parseInt(quantity)); // Convert quantity to integer

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
}
