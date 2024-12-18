package com.example.foodorderingapp.classes;

public class OrderItem {
    private String name;
    private int quantity;
    private int price;

    public OrderItem(String name, int quantity, int price) {
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters
    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getPrice() {
        return price;
    }
}