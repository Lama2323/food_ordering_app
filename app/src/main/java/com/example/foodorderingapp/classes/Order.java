package com.example.foodorderingapp.classes;

import java.io.Serializable;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Order implements Serializable {
    private String objectId;
    private String address;
    private String food_list;  // JSON string
    private String phone_number;
    private int total;
    private int payment_method;

    public Order() {}

    public Order(String address, List<Cart> cartItems, String phone_number, int total, int payment_method) {
        this.address = address;
        this.phone_number = phone_number;
        this.total = total;
        this.payment_method = payment_method;
        setFoodListFromCart(cartItems);
    }

    private void setFoodListFromCart(List<Cart> cartItems) {
        try {
            JSONArray foodArray = new JSONArray();
            for (Cart item : cartItems) {
                JSONObject foodItem = new JSONObject();
                foodItem.put("name", item.getName());
                foodItem.put("quantity", item.getQuantity());
                foodArray.put(foodItem);
            }
            this.food_list = foodArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            // Set a default empty array in case of error
            this.food_list = "[]";
        }
    }

    // Getters and setters
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFood_list() {
        return food_list;
    }

    public void setFood_list(String food_list) {
        this.food_list = food_list;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPayment_method() {
        return payment_method;
    }

    public void setPayment_method(int payment_method) {
        this.payment_method = payment_method;
    }
}