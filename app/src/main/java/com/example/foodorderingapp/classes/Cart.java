package com.example.foodorderingapp.classes;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import java.io.Serializable;

public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    private String image_source;
    private String name;
    private int price;
    private int quantity;
    private boolean isChecked;
    private String objectId;

    public Cart() {}

    public Cart(String image_source, String name, int price, int quantity) {
        this.image_source = image_source;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public void setImage_source(String image_source) {
        this.image_source = image_source;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImage_source() {return image_source; }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getTotal_price() {
        return quantity * price;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public static void addToCart(Cart cartItem) {
        System.out.println("Saving cart item: " + cartItem);
        Backendless.Data.of(Cart.class).save(cartItem, new AsyncCallback<Cart>() {
            @Override
            public void handleResponse(Cart response) {
                // Xử lý khi thêm thành công
                System.out.println("Đã thêm món ăn vào giỏ hàng: " + response.getName());
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // Xử lý khi có lỗi
                System.out.println("Lỗi khi thêm vào giỏ hàng: " + fault.getMessage());
            }
        });
    }
}
