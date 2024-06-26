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

public class OrdersPnLActivity extends MainActivity {


    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);
        String tradingSymbol = getIntent().getStringExtra("Trading_Symbol");
        LinearLayout ordersLayout = findViewById(R.id.stock_layout);


        dbHelper = new DatabaseHelper(this);
        TextView headingTextView = findViewById(R.id.orders_heading);
        headingTextView.setText("Orders: "+ tradingSymbol);


        displayOrders(ordersLayout,tradingSymbol);

        Button watchlistButton = findViewById(R.id.btn_watchlist);
        watchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrdersPnLActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

        Button ordersButton = findViewById(R.id.btn_orders);
        ordersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrdersPnLActivity.this, OrdersActivity.class);
                startActivity(intent);
            }
        });
        Button pnlButton = findViewById(R.id.btn_pnl);
        pnlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrdersPnLActivity.this, PnLActivity.class);
                startActivity(intent);
            }
        });
    }
    public void displayOrders(LinearLayout layout,String tradingSymbol) {
        layout.removeAllViews(); // Clear the layout before adding new views
        List<Order> orders = dbHelper.getExecutedOrdersForSymbol(tradingSymbol);

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
                StringBuilder orderInfo = new StringBuilder();
                orderInfo.append("Trading Symbol: ").append(order.getTradingSymbol()).append("\n");
                orderInfo.append("Price: ").append(order.getPrice()).append("\n");
                orderInfo.append("Status: ").append(order.getStatus()).append("\n"); // Set status to pending for now
                orderInfo.append("Quantity: ").append(order.getQuantity()).append("\n");
                orderInfo.append("Type: ").append(order.getType()).append("\n"); // Buy or Sell
                orderInfo.append("Time: ").append(order.getTime_stamp());

                textView.setText(orderInfo.toString());
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

}
