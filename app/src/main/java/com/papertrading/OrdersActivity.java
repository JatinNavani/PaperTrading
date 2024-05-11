package com.papertrading;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class OrdersActivity extends MainActivity {


    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        LinearLayout ordersLayout = findViewById(R.id.stock_layout);


        dbHelper = new DatabaseHelper(this);

        displayOrders(ordersLayout);

        Button watchlistButton = findViewById(R.id.btn_watchlist);
        watchlistButton.setOnClickListener(view -> {
            showWatchlist();
        });

        Button ordersButton = findViewById(R.id.btn_orders);
        ordersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrdersActivity.this, OrdersActivity.class);
                startActivity(intent);
            }
        });
    }
    private void displayOrders(LinearLayout layout) {
        layout.removeAllViews(); // Clear the layout before adding new views
        List<Order> orders = dbHelper.getAllOrders();

        // Check if there are stocks to display
        if (orders.isEmpty()) {
            // Display a message indicating no stocks found
            TextView noOrdersTextView = new TextView(this);
            noOrdersTextView.setText("No orders found");
            noOrdersTextView.setTextSize(16);
            noOrdersTextView.setPadding(16, 8, 16, 8);
            layout.addView(noOrdersTextView);
        } else {
            // Add views for each stock and divider
            for (Order order : orders) {
                TextView textView = new TextView(this);
                textView.setText(order.getTradingSymbol() + ": " + order.getOrderType());

                // Add an OnClickListener to each order TextView (existing code)
                textView.setOnClickListener(view -> {
                    // Display order info


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

    /*private void displayOrders() {
        // Retrieve orders from the database
        List<Order> orders = dbHelper.getAllOrders();

        // Display the trading symbols of orders in descending order of order ID
        StringBuilder orderText = new StringBuilder();
        for (Order order : orders) {
            orderText.append(order.getTradingSymbol()).append("\n");
        }
        ordersTextView.setText(orderText.toString());
    }

     */
}
