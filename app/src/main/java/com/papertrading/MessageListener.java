package com.papertrading;

public interface MessageListener {
    void onPriceUpdateReceived(long instrumentToken, double price);


}

