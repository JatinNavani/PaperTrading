package com.papertrading;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PnLActivity extends OrdersActivity implements MessageListener {
    private DatabaseHelper dbHelper;
    private MainActivity mainActivity;
    private Map<String, List<Order>> orderMap = new HashMap<>();
    private Map<String, TextView> pnlTextViewMap = new HashMap<>();

    TextView overallPnLTextView;



    private static ConcurrentHashMap<String,Double> overallPnlCache = new ConcurrentHashMap();
    private LinearLayout ordersLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pnl);
        ordersLayout = findViewById(R.id.stock_layout);


        dbHelper = new DatabaseHelper(this);


        RabbitMQConnection.registerEventListener(this);



        List<Order> orders = dbHelper.getAllOrders();
        orderMap = groupOrdersBySymbol(orders);

        ordersLayout.removeAllViews();

        displayPositions(ordersLayout);

        setupButtons();



    }

    @Override
    public void onPriceUpdateReceived(long instrumentToken, double price) {
        System.out.println("Price in PnL Activity");
        String tradingSymbol = dbHelper.getTradingSymbolByInstrumentToken(instrumentToken);

        updatePnL(tradingSymbol, price);


    }

    private void setupButtons() {
        Button watchlistButton = findViewById(R.id.btn_watchlist);
        watchlistButton.setOnClickListener(v -> {
            Intent intent = new Intent(PnLActivity.this, MainActivity.class);
            startActivity(intent);
        });

        Button ordersButton = findViewById(R.id.btn_orders);
        ordersButton.setOnClickListener(v -> {
            Intent intent = new Intent(PnLActivity.this, OrdersActivity.class);
            startActivity(intent);
        });

        Button pnlButton = findViewById(R.id.btn_pnl);
        pnlButton.setOnClickListener(v -> {
            Intent intent = new Intent(PnLActivity.this, PnLActivity.class);
            startActivity(intent);
        });
    }

    private void displayPositions(LinearLayout layout) {
         // Clear the layout before adding new views


        for (Map.Entry<String, List<Order>> entry : orderMap.entrySet()) {
            String tradingSymbol = entry.getKey();
            List<Order> symbolOrders = entry.getValue();
            int position = calculateNetPositions(symbolOrders);

            LinearLayout symbolLayout = new LinearLayout(this);
            symbolLayout.setOrientation(LinearLayout.VERTICAL);
            symbolLayout.setPadding(10, 10, 10, 10);

            if (position == 0) {
                symbolLayout.setBackgroundColor(Color.LTGRAY); // Set background to grey for closed positions
            }

            TextView tradingSymbolTextView = new TextView(this);
            tradingSymbolTextView.setText(tradingSymbol);
            symbolLayout.addView(tradingSymbolTextView);


            tradingSymbolTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Retrieve the list of executed orders for the selected stock
                    Intent intent = new Intent(PnLActivity.this, OrdersPnLActivity.class);
                    intent.putExtra("Trading_Symbol", tradingSymbol);
                    startActivity(intent);
                }
            });

            TextView quantityTextView = new TextView(this);
            quantityTextView.setText("Holding Quantity: " + position); // Display the quantity
            symbolLayout.addView(quantityTextView);

            TextView positionTextView = new TextView(this);
            positionTextView.setText(position != 0 ? "Position : Open" : "Position : Closed");
            symbolLayout.addView(positionTextView);

            // Add a TextView for PnL and store it in the map
            TextView pnlTextView = new TextView(this);
            if (position == 0) {
                double closedPnL = calculateProfitLoss(symbolOrders, 0); // Assuming closed positions use the last known price
                pnlTextView.setText(String.format(Locale.getDefault(), " PnL: ₹%.2f", closedPnL));
                overallPnlCache.put(tradingSymbol,closedPnL);
            }
            if (RabbitMQConnection.getPricesCache().containsKey(dbHelper.getInstrumentTokenByTradingSymbol(tradingSymbol))){
                double currentPrice = RabbitMQConnection.getPricesCache().get(dbHelper.getInstrumentTokenByTradingSymbol(tradingSymbol));
                double openPnL = calculateProfitLoss(symbolOrders, currentPrice); // Calculate PnL using current price
                pnlTextView.setText(String.format(Locale.getDefault(), " PnL: ₹%.2f", openPnL));
                overallPnlCache.put(tradingSymbol,openPnL);

            }else{
                pnlTextView.setText("PnL: Will be updated soon");
                pnlTextViewMap.put(tradingSymbol, pnlTextView);
            }
            symbolLayout.addView(pnlTextView);

            pnlTextViewMap.put(tradingSymbol, pnlTextView);

            layout.addView(symbolLayout);

            // Add a horizontal line
            View divider = new View(this);
            divider.setBackgroundColor(Color.GRAY);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 2)); // Adjust height as needed
            layout.addView(divider);
        }
        double overallPnL = 0;
        for (double pnl : overallPnlCache.values()) {
            overallPnL += pnl;
        }
        // Add a rectangular box to display overall PnL
        overallPnLTextView = new TextView(this);
        overallPnLTextView.setText(String.format(Locale.getDefault(), "Overall PnL: ₹%.2f", overallPnL));
        overallPnLTextView.setBackgroundColor(Color.WHITE);
        overallPnLTextView.setTextColor(Color.BLACK);
        overallPnLTextView.setPadding(20, 10, 20, 10); // Adjust padding as needed
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 10, 100); // Adjust margins as needed
        overallPnLTextView.setLayoutParams(params);
        params.gravity = Gravity.BOTTOM | Gravity.END; // Set gravity to bottom right
        overallPnLTextView.setLayoutParams(params);
        layout.addView(overallPnLTextView);


    }

    private Map<String, List<Order>> groupOrdersBySymbol(List<Order> orders) {
        Map<String, List<Order>> orderMap = new HashMap<>();
        for (Order order : orders) {
            if ("Executed".equalsIgnoreCase(order.getStatus())) {
                String symbol = order.getTradingSymbol();
                orderMap.computeIfAbsent(symbol, k -> new ArrayList<>()).add(order);
            }
        }
        return orderMap;
    }

    private int calculateNetPositions(List<Order> orders) {
        int netPosition = 0;
        for (Order order : orders) {
            if (order.getType().equalsIgnoreCase("buy")) {
                netPosition += order.getQuantity();
            } else if (order.getType().equalsIgnoreCase("sell")) {
                netPosition -= order.getQuantity();
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

        int closedQuantity = Math.min(totalBoughtQuantity, totalSoldQuantity);
        double averageBuyPrice = totalBoughtQuantity > 0 ? totalBuyValue / totalBoughtQuantity : 0;
        double averageSellPrice = totalSoldQuantity > 0 ? totalSellValue / totalSoldQuantity : 0;
        realizedProfit = closedQuantity * (averageSellPrice - averageBuyPrice);

        int openQuantity = totalBoughtQuantity - totalSoldQuantity;
        double unrealizedProfit = openQuantity > 0 ? openQuantity * (currentPrice - averageBuyPrice) : 0;

        return realizedProfit + unrealizedProfit;
    }

    protected void updatePnL(String tradingSymbol, double currentPrice) {

        if (orderMap.containsKey(tradingSymbol)) {
            List<Order> orders = orderMap.get(tradingSymbol);
            double profitLoss = calculateProfitLoss(orders, currentPrice);
            String newText = String.format(Locale.getDefault(), "PnL: ₹%.2f", profitLoss);
            overallPnlCache.put(tradingSymbol,profitLoss);
            if (pnlTextViewMap.containsKey(tradingSymbol)) {
                pnlTextViewMap.get(tradingSymbol).setText(newText);
            }
            double overallPnL = 0;
            for (double pnl : overallPnlCache.values()) {
                overallPnL += pnl;
            }
            // Add a rectangular box to display overall PnL
            /*
            TextView overallPnLTextView = findViewById(R.id.total_profit_text_view);
            overallPnLTextView.setText(String.format(Locale.getDefault(), "Overall PnL: ₹%.2f", overallPnL));

             */

            overallPnLTextView.setText(String.format(Locale.getDefault(), "Overall PnL: ₹%.2f", overallPnL));

        }
    }
    public static ConcurrentHashMap<String, Double> getOverallPnlCache() {
        return overallPnlCache;
    }

    public static void setOverallPnlCache(ConcurrentHashMap<String, Double> overallPnlCache) {
        PnLActivity.overallPnlCache = overallPnlCache;
    }
}
