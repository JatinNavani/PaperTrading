package com.papertrading;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PnLActivity extends OrdersActivity implements RabbitMQConnection.MessageListener {
    private DatabaseHelper dbHelper;
    private RabbitMQConnection rbmqconnect1;
    private List<Position> openPositions = new ArrayList<>();
    private double currentPrice=0;
    private Map<String, List<Order>> orderMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pnl);
        LinearLayout ordersLayout = findViewById(R.id.stock_layout);


        dbHelper = new DatabaseHelper(this);

        rbmqconnect1 = new RabbitMQConnection(dbHelper, ordersLayout);
        rbmqconnect1.setMessageListener(this);
        rbmqconnect1.startConsuming();

        displayPositions(ordersLayout,currentPrice);

        List<Order> orders = dbHelper.getAllOrders();
        orderMap = groupOrdersBySymbol(orders);

        Button watchlistButton = findViewById(R.id.btn_watchlist);
        watchlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PnLActivity.this, MainActivity.class);
                startActivity(intent);

            }
        });

        Button ordersButton = findViewById(R.id.btn_orders);
        ordersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PnLActivity.this, OrdersActivity.class);
                startActivity(intent);
            }
        });

        Button pnlButton = findViewById(R.id.btn_pnl);
        pnlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PnLActivity.this, PnLActivity.class);
                startActivity(intent);
            }
        });


    }
    @Override
    public void onPriceUpdateReceived(long instrumentToken, double price) {
        String tradingSymbol = dbHelper.getTradingSymbolByInstrumentToken(instrumentToken);
        if (tradingSymbol != null) {
            updatePnL(tradingSymbol, price);
        }


    }



    private void displayPositions(LinearLayout layout,double currentPrice) {
        layout.removeAllViews(); // Clear the layout before adding new views

        List<Order> orders = dbHelper.getAllOrders();
        Map<String, List<Order>> orderMap = groupOrdersBySymbol(orders);


        for (Map.Entry<String, List<Order>> entry : orderMap.entrySet()) {
            String tradingSymbol = entry.getKey();
            List<Order> symbolOrders = entry.getValue();
            int position = calculateNetPositions(symbolOrders);


            TextView tradingSymbolTextView = new TextView(this);
            tradingSymbolTextView.setText(tradingSymbol );
            layout.addView(tradingSymbolTextView);



            tradingSymbolTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Retrieve the list of executed orders for the selected stock
                    List<Order> executedOrders = dbHelper.getExecutedOrdersForSymbol(tradingSymbol);
                    // Display the list of executed orders
                    displayExecutedOrders(executedOrders);
                }
            });
            TextView quantityTextView = new TextView(this);
            quantityTextView.setText("Holding Quantity: " + position); // Display the quantity
            layout.addView(quantityTextView);

            TextView positionTextView = new TextView(this);
            if (position != 0) {
                positionTextView.setText("Position : Open");
            } else {
                positionTextView.setText("Position : Closed");
            }
            layout.addView(positionTextView);




            // Add a horizontal line
            View divider = new View(this);
            divider.setBackgroundColor(Color.GRAY);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2)); // Adjust height as needed
            layout.addView(divider);
        }
    }

    private Map<String, List<Order>> groupOrdersBySymbol(List<Order> orders) {
        /*
        Map<String, List<Order>> orderMap = new HashMap<>();
        for (Order order : orders) {
            String symbol = order.getTradingSymbol();
            if (!orderMap.containsKey(symbol)) {
                orderMap.put(symbol, new ArrayList<>());
            }
            orderMap.get(symbol).add(order);
        }
        return orderMap;
        */
        Map<String, List<Order>> orderMap = new HashMap<>();
        for (Order order : orders) {
            // Only include orders with status "Executed"
            if ("Executed".equalsIgnoreCase(order.getStatus())) {
                String symbol = order.getTradingSymbol();
                if (!orderMap.containsKey(symbol)) {
                    orderMap.put(symbol, new ArrayList<>());
                }
                orderMap.get(symbol).add(order);
            }
        }
        return orderMap;
    }

    private int calculateNetPositions(List<Order> orders) {
        int netPosition = 0;
        for (Order order : orders) {
            String type = order.getType();
            int quantity = order.getQuantity();
            if (type.equalsIgnoreCase("buy")) {
                netPosition += quantity;
            } else if (type.equalsIgnoreCase("sell")) {
                netPosition -= quantity;
            }
        }
        return netPosition;
    }





    private double calculateProfitLoss(List<Order> orders, double currentPrice) {
        double realizedProfit = 0;
        int totalBoughtQuantity = 0;
        double totalBuyValue = 0;
        int totalSoldQuantity = 0;
        double totalSellValue = 0;

        for (Order order : orders) {
            if (order.getType().equalsIgnoreCase("buy")) {
                totalBuyValue += order.getQuantity() * order.getPrice();
                totalBoughtQuantity += order.getQuantity();
            } else if (order.getType().equalsIgnoreCase("sell")) {
                totalSellValue += order.getQuantity() * order.getPrice();
                totalSoldQuantity += order.getQuantity();
            }
        }

        // Realized profit calculation for closed portions of the positions
        int closedQuantity = Math.min(totalBoughtQuantity, totalSoldQuantity);
        double averageBuyPrice = totalBoughtQuantity > 0 ? totalBuyValue / totalBoughtQuantity : 0;
        double averageSellPrice = totalSoldQuantity > 0 ? totalSellValue / totalSoldQuantity : 0;
        realizedProfit = closedQuantity * (averageSellPrice - averageBuyPrice);

        // Unrealized profit for the open positions
        int openQuantity = totalBoughtQuantity - totalSoldQuantity;
        double unrealizedProfit = openQuantity > 0 ? openQuantity * (currentPrice - averageBuyPrice) : 0;

        // Total profit is the sum of realized and unrealized profits
        return realizedProfit + unrealizedProfit;
    }

/*
    protected void updatePnL(long instrumentToken, double currentPrice) {
        String tradingSymbol = dbHelper.getTradingSymbolByInstrumentToken(instrumentToken);

        if (tradingSymbol != null && orderMap.containsKey(tradingSymbol)) {
            List<Order> orders = orderMap.get(tradingSymbol);
            // Update current price TextView
            for (int i = 0; i < stockLayout.getChildCount(); i++) {
                View view = stockLayout.getChildAt(i);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    String text = textView.getText().toString();
                    if (text.contains(tradingSymbol)) {
                        // Update the price dynamically
                        textView.setText(tradingSymbol + " - Current Price: ₹" + currentPrice);
                        break;
                    }
                }
            }
            double profitLoss = calculateProfitLoss(orders, currentPrice); // Use the modified method that considers real-time price

            for (int i = 0; i < stockLayout.getChildCount(); i++) {
                View view = stockLayout.getChildAt(i);
                if (view instanceof TextView) {
                    TextView textView = (TextView) view;
                    if (textView.getText().toString().startsWith(tradingSymbol)) {
                        // Assume your TextView's text is formatted as "Symbol - PnL: $xxx"
                        String newText = String.format(Locale.getDefault(), "%s - PnL: ₹%.2f", tradingSymbol, profitLoss);
                        textView.setText(newText);
                        break;
                    }
                }
            }
        }



    }

 */
protected void updatePnL(String tradingSymbol, double currentPrice) {
    LinearLayout ordersLayout = findViewById(R.id.stock_layout);
    if (orderMap.containsKey(tradingSymbol)) {
        List<Order> orders = orderMap.get(tradingSymbol);
        for (int i = 0; i < ordersLayout.getChildCount(); i++) {
            View view = ordersLayout.getChildAt(i);
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                String text = textView.getText().toString();
                if (text.contains(tradingSymbol)) {
                    textView.setText(tradingSymbol + " - Current Price: ₹" + currentPrice);
                    double profitLoss = calculateProfitLoss(orders, currentPrice);
                    String newText = String.format(Locale.getDefault(), "%s - PnL: ₹%.2f", tradingSymbol, profitLoss);
                    textView.setText(newText);
                } else if (text.startsWith(tradingSymbol)) {
                    double profitLoss = calculateProfitLoss(orders, currentPrice);
                    String newText = String.format(Locale.getDefault(), "%s - PnL: ₹%.2f", tradingSymbol, profitLoss);
                    textView.setText(newText);
                }
            }
        }
    }
}
    private void displayExecutedOrders(List<Order> executedOrders) {
        // Create a dialog or start a new activity to display the executed orders
        // Example: display in a dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Executed Orders");
        // Create a StringBuilder to build the message
        StringBuilder message = new StringBuilder();
        for (Order order : executedOrders) {
            // Append order details to the message
            message.append(order.toString()).append("\n");
        }
        builder.setMessage(message.toString());
        // Add a button to dismiss the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        // Show the dialog
        builder.show();
    }
}


