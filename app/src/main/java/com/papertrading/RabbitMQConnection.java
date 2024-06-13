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
    import com.rabbitmq.client.Delivery;
    import com.rabbitmq.client.ShutdownListener;
    import com.rabbitmq.client.ShutdownSignalException;

    import java.io.IOException;
    import java.nio.charset.StandardCharsets;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.concurrent.TimeoutException;

    public class RabbitMQConnection extends AsyncTask<Void, Void, Void>{

        private static final String USERNAME = "test";
        private static final String PASSWORD = "test";
        //private static final String HOSTNAME = "192.168.1.6";
        private static final String HOSTNAME = "papertrade.onthewifi.com";
        private static final int PORT = 5672; // Default RabbitMQ port
        private static final String QUEUE_NAME = "YoQueue";

        private static ArrayList<MessageListener> eventListeners = new ArrayList<MessageListener>();



        private static ConcurrentHashMap<Long,Double> pricesCache = new ConcurrentHashMap();

        private LinearLayout stockLayout;
        private static DatabaseHelper dbHelper;
        private Stock stock;



        public RabbitMQConnection(DatabaseHelper dbHelper, LinearLayout stockLayout) {
            this.dbHelper = dbHelper;
            this.stockLayout = stockLayout;

        }

        @Override
        protected Void  doInBackground(Void... voids) {
            connectMQ();
            return null;
        }





            public void connectMQ() {
                // Your subscription logic goes here
                try {
                    System.out.println("Connect MQ CALLED");
                    ConnectionFactory factory = new ConnectionFactory();
                    factory.setHost(HOSTNAME);
                    factory.setPort(PORT);
                    factory.setUsername(USERNAME);
                    factory.setPassword(PASSWORD);
                    Connection connection = factory.newConnection();

                    connection.addShutdownListener(new ShutdownListener() {
                        public void shutdownCompleted(ShutdownSignalException cause) {
                            System.out.println("Connection closed by a reason" + cause.toString());
                        }
                    });

                    Channel channel = connection.createChannel();

                    // Declare a queue for the instrument token
                    String queueName = channel.queueDeclare("device_" + dbHelper.retrieveUniqueId(), false, false, false, null).getQueue();

                    // Bind the queue to the exchange with the routing key based on the instrument token
                    String routingKey = "device." + dbHelper.retrieveUniqueId();
                    channel.queueBind(queueName, "YoExchange", routingKey);
                    //UpdateTask updateTask = new UpdateTask();
                    // Start consuming messages from the queue
                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        System.out.println("recv mesg" + delivery);
                        (new UpdateTask()).execute(delivery);

                    };
                    channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                    });

                } catch (IOException | TimeoutException e) {
                    Log.e("RabbitMQConnection", "Error occurred while subscribing to token " +  e);
                }
            }




        public static void updatePricesCache(long instrumentToken, double price) {
            pricesCache.put(instrumentToken, price);
        }

        public static ConcurrentHashMap<Long, Double> getPricesCache() {
            return pricesCache;
        }

        public static void setPricesCache(ConcurrentHashMap<Long, Double> pricesCache) {
            RabbitMQConnection.pricesCache = pricesCache;
        }

        public static void registerEventListener(MessageListener msgListener) {
                if (!eventListeners.contains(msgListener)) {
                   eventListeners.add(msgListener);
            }
        }

        public static ArrayList<MessageListener> getRegisterEventListener() {
            return eventListeners;

        }

    }


