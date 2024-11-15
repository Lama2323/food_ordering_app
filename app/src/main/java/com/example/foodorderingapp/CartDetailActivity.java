package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.bumptech.glide.Glide;
import com.example.foodorderingapp.classes.Cart;

public class CartDetailActivity extends AppCompatActivity {
    private Cart cartItem;
    private TextView quantityTextView;
    private TextView totalPriceTextView;
    private int currentQuantity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart_detail);

        // Get cart item from intent
        cartItem = (Cart) getIntent().getSerializableExtra("cartItem");
        if (cartItem == null) {
            Toast.makeText(this, "Error: Cart item not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentQuantity = cartItem.getQuantity();

        // Initialize views
        ImageButton backButton = findViewById(R.id.button_back);
        ImageView productImage = findViewById(R.id.product_image);
        TextView productName = findViewById(R.id.product_name);
        TextView productPrice = findViewById(R.id.product_price);
        ImageButton decreaseButton = findViewById(R.id.button_decrease);
        ImageButton increaseButton = findViewById(R.id.button_increase);
        quantityTextView = findViewById(R.id.quantity);
        totalPriceTextView = findViewById(R.id.total_price);
        Button okButton = findViewById(R.id.button_ok);

        // Set data to views
        Glide.with(this)
                .load(cartItem.getImage_source())
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(productImage);

        productName.setText(cartItem.getName());
        productPrice.setText("Giá: " + cartItem.getPrice());
        updateQuantityAndTotal();

        // Set click listeners
        backButton.setOnClickListener(v -> finish());

        decreaseButton.setOnClickListener(v -> {
            if (currentQuantity > 1) {
                currentQuantity--;
                updateQuantityAndTotal();
            }
        });

        increaseButton.setOnClickListener(v -> {
            currentQuantity++;
            updateQuantityAndTotal();
        });

        okButton.setOnClickListener(v -> updateCartItem());
    }

    private void updateQuantityAndTotal() {
        quantityTextView.setText(String.valueOf(currentQuantity));
        int totalPrice = currentQuantity * cartItem.getPrice();
        totalPriceTextView.setText("Tổng tiền: " + totalPrice);
    }

    private void updateCartItem() {
        cartItem.setQuantity(currentQuantity);

        Backendless.Data.of(Cart.class).save(cartItem, new AsyncCallback<Cart>() {
            @Override
            public void handleResponse(Cart response) {
                // Send result back to CartActivity
                Intent resultIntent = new Intent();
                resultIntent.putExtra("updatedCartItem", response);
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(CartDetailActivity.this,
                        "Error updating cart: " + fault.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}