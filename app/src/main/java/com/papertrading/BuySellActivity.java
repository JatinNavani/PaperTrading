package com.papertrading;



import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BuySellActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_sell);

        String tradingSymbol = getIntent().getStringExtra("Trading_Symbol");

        Button buyButton = findViewById(R.id.buy_button);
        Button sellButton = findViewById(R.id.sell_button);
        TextView stockNameTextView = findViewById(R.id.trading_symbol_text_view);
        stockNameTextView.setText(tradingSymbol);

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle buy action
                // You can add your logic here
                Toast.makeText(BuySellActivity.this, "Buy: " + tradingSymbol, Toast.LENGTH_SHORT).show();
            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle sell action
                // You can add your logic here
                Toast.makeText(BuySellActivity.this, "Sell: " + tradingSymbol, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
