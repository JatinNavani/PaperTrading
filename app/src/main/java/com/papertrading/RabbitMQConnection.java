package com.papertrading;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class RabbitMQConnection {

    private static final String Username = "jatin";
    private static final String PASSWORD = "jatin";


    private static final String QUEUE_NAME = "HelloQueue1";
    private static final String HOSTNAME = "192.168.1.5";
    private static final int PORT = 5672; // Default RabbitMQ port

    public void startConsuming() {
        new RabbitMQAsyncTask().execute();
    }

    private static class RabbitMQAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                ConnectionFactory factory = new ConnectionFactory();
                factory.setHost(HOSTNAME);
                factory.setPort(PORT);
                factory.setUsername(Username);
                factory.setPassword(PASSWORD);
                //factory.setVirtualHost("/");
                Connection connection = factory.newConnection();
                Channel channel = connection.createChannel();

                channel.queueDeclare("HelloQueue1", true, false, false, null);
                System.out.println("Waiting for messages.");

                channel.basicConsume("HelloQueue1", true, (consumerTag, message) -> {
                    String m = new String(message.getBody(), "UTF-8");
                    System.out.println("Just Received =" + m);

                }, consumerTag -> {
                });
            } catch (IOException | TimeoutException e) {
                Log.e("RabbitMQConnection", "Error occurred while consuming messages", e);
            }
            return null;
        }

    }
}
