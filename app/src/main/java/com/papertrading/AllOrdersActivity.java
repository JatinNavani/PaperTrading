package com.papertrading;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AllOrdersActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private String clickedTradingSymbol;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_orders);

        dbHelper = new DatabaseHelper(this);

        // Get the clicked trading symbol from the intent
        clickedTradingSymbol = getIntent().getStringExtra("clickedTradingSymbol");

        displayExecutedOrdersForSymbol();
    }

    private void displayExecutedOrdersForSymbol() {
        LinearLayout layout = findViewById(R.id.order_layout);
        layout.removeAllViews(); // Clear the layout before adding new views

        // Retrieve executed orders for the clicked trading symbol
        List<Order> executedOrders = dbHelper.getExecutedOrdersForSymbol(clickedTradingSymbol);

        // Display the executed orders
        for (Order order : executedOrders) {
            TextView textView = new TextView(this);
            textView.setText(order.toString());
            layout.addView(textView);

            // Add a horizontal line
            View divider = new View(this);
            divider.setBackgroundColor(Color.GRAY);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2)); // Adjust height as needed
            layout.addView(divider);
        }
    }

    private void displayOrderDetails(Order order) {
        // Create a dialog to display the order details
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Order Details");
        builder.setMessage(order.toString());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
}