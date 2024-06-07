// Order.java

package com.papertrading;

public class Order {
    private long id;
    private double price;
    private String type;
    private long instrumentToken;
    private String name;
    private long exchangeToken;
    private String tradingSymbol;
    private String exchange;
    private String status;
    private int quantity;



    public String getOrderType() {
        return type;
    }

    public void setOrderType(String type) {
        this.type = type;
    }

    public Order(long id, double price, String type, long instrumentToken, String name, long exchangeToken, String tradingSymbol, String exchange, int quantity,String status) {
        this.id = id;
        this.price = price;
        this.type = type;
        this.instrumentToken = instrumentToken;
        this.name = name;
        this.exchangeToken = exchangeToken;
        this.tradingSymbol = tradingSymbol;
        this.exchange = exchange;
        this.quantity = quantity;
        this.status = status;
    }


    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTradingSymbol() {
        return tradingSymbol;
    }

    public void setTradingSymbol(String tradingSymbol) {
        this.tradingSymbol = tradingSymbol;
    }

    public double getPrice() {
        return price;
    }
    public String getStatus() {
        return status;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    public void setStatus(String status) {
        this.status = status;
    }


}
