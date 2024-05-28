package com.papertrading;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnection {

    private static final String USERNAME = "jatin";
    private static final String PASSWORD = "jatin";
    private static final String HOSTNAME = "192.168.1.6";
    private static final int PORT = 5672; // Default RabbitMQ port
    private static final String QUEUE_NAME = "dummyQueue";

    private LinearLayout stockLayout;
    private DatabaseHelper dbHelper;
    private MessageListener messageListener;
    private Stock stock;

    public interface MessageListener {
        void onPriceUpdateReceived(long instrumentToken, double price);
    }

    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    public RabbitMQConnection(DatabaseHelper dbHelper, LinearLayout stockLayout) {
        this.dbHelper = dbHelper;
        this.stockLayout = stockLayout;
    }

    public void startConsuming() {
        new RabbitMQAsyncTask().execute();
    }

    private class RabbitMQAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(HOSTNAME);
                factory.setPort(PORT);
                factory.setUsername(USERNAME);
                factory.setPassword(PASSWORD);
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.queueDeclare(QUEUE_NAME, true, false, false, null);

                DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                    System.out.println("Jatin");

                    String messageBody = new String(delivery.getBody(), StandardCharsets.UTF_8);
                    Gson gson = new Gson();
                    PricePayload payload = gson.fromJson(messageBody, PricePayload.class);

                    if (payload != null) {
                        Log.d("RabbitMQConnection", "Received price update for " + payload.getInstrumentToken() + ": " + payload.getPrice());
                        updateStockPrice(payload.getInstrumentToken(),payload.getPrice());
                    }
                };

                channel.basicConsume("dummyQueue", true, deliverCallback, consumerTag -> { });
            } catch (IOException | TimeoutException e) {
                Log.e("RabbitMQConnection", "Error occurred while consuming messages", e);
            }
            return null;
        }
    }

    private void updateStockPrice(long instrumentToken, double price) {
        // Notify the MainActivity about the price upate
        if (messageListener != null) {
            messageListener.onPriceUpdateReceived(instrumentToken, price);
        }
    }
}
