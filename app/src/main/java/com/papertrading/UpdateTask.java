package com.papertrading;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.rabbitmq.client.Delivery;

import java.nio.charset.StandardCharsets;

public class UpdateTask extends AsyncTask<Delivery, Void, Void> {



    @Override
    protected Void doInBackground(Delivery... delivery) {

        String messageBody = new String(delivery[0].getBody(), StandardCharsets.UTF_8);
        Gson gson = new Gson();
        PricePayload payload = gson.fromJson(messageBody, PricePayload.class);
        if (payload != null) {

            Log.d("RabbitMQConnection", "Received price update for " + payload.getInstrumentToken() + ": " + payload.getPrice());
            // Update the prices cache

            RabbitMQConnection.updatePricesCache(payload.getInstrumentToken(), payload.getPrice());
            System.out.println("RabbitMQConnection"+ "Cache price update for " + payload.getInstrumentToken() + ": " + payload.getPrice());


            for (MessageListener listener : RabbitMQConnection.getRegisterEventListener()) {
                listener.onPriceUpdateReceived(payload.getInstrumentToken(), payload.getPrice());
            }

        }


        return null;
    }
}



