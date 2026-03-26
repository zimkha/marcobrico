package com.mmd.marcobrico.domain;


import lombok.Getter;


public class Order {
    private final String client;
    private final double amount;
    private final boolean paid;

    public Order(String client, double amount, boolean paid) {
        this.client = client;
        this.amount = amount;
        this.paid = paid;
    }

    public double getAmount() { return amount; }
    public boolean isPaid() { return paid; }

}
