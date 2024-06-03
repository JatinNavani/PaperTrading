package com.papertrading;

public class Position {
    private String symbol;
    private int quantity;
    private double averageBuyPrice;
    private double currentPrice;

    public Position(String symbol, int quantity, double averageBuyPrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageBuyPrice = averageBuyPrice;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getAverageBuyPrice() {
        return averageBuyPrice;
    }

    public void setAverageBuyPrice(double averageBuyPrice) {
        this.averageBuyPrice = averageBuyPrice;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }
// Getter and setter methods

    public double calculateProfit(double currentPrice) {
        return (currentPrice - averageBuyPrice) * quantity;
    }
}