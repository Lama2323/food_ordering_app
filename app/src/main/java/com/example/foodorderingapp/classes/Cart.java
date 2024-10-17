package com.example.foodorderingapp.classes;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class Cart {
    private String food_image;
    private String food_name;
    private int price;
    private int quantity;
    private int total_price;

    public Cart() {}

    public Cart(String food_image, String food_name, int price, int quantity) {
        this.food_image = food_image;
        this.food_name = food_name;
        this.price = price;
        this.quantity = quantity;
        this.total_price = price * quantity;
    }

    public void setFood_image(String food_image) {
        this.food_image = food_image;
    }

    public void setFood_name(String food_name) {
        this.food_name = food_name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setTotal_price(int total_price) {
        this.total_price = total_price;
    }

    public String getFood_image() {
        return food_image;
    }

    public String getFood_name() {
        return food_name;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getTotal_price() {
        return total_price;
    }

    public static void addToCart(Cart cartItem) {
        System.out.println("Saving cart item: " + cartItem);
        Backendless.Data.of(Cart.class).save(cartItem, new AsyncCallback<Cart>() {
            @Override
            public void handleResponse(Cart response) {
                // Xử lý khi thêm thành công
                System.out.println("Đã thêm món ăn vào giỏ hàng: " + response.getFood_name());
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // Xử lý khi có lỗi
                System.out.println("Lỗi khi thêm vào giỏ hàng: " + fault.getMessage());
            }
        });
    }
}
