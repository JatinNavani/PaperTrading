package com.papertrading;

public class Stock {
    private String name;
    private String tradingSymbol;



    private String exchange;
    private double lastPrice;



    private int exchange_token;

    private long instrumentToken;


    private boolean inWatchlist;

    public Stock(String name,String tradingSymbol, double lastPrice) {
        this.name = name;
        this.tradingSymbol = tradingSymbol;
        this.lastPrice = lastPrice;
    }
    public Stock(String name,String tradingSymbol, double lastPrice, boolean inWatchlist) {
        this.name = name;
        this.tradingSymbol = tradingSymbol;
        this.lastPrice = lastPrice;
        this.inWatchlist = inWatchlist;
    }

    // Getters and setters for tradingSymbol and lastPrice
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }
    public boolean isInWatchlist() {
        return inWatchlist;
    }

    public void setInWatchlist(boolean inWatchlist) {
        this.inWatchlist = inWatchlist;
    }
    public long getInstrumentToken() {
        return instrumentToken;
    }

    public void setInstrumentToken(long instrumentToken) {
        this.instrumentToken = instrumentToken;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }
    public int getExchange_token() {
        return exchange_token;
    }

    public void setExchange_token(int exchange_token) {
        this.exchange_token = exchange_token;
    }
}
