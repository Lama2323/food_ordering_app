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
import com.backendless.persistence.DataQueryBuilder;
import com.bumptech.glide.Glide;
import com.example.foodorderingapp.classes.Cart;
import com.example.foodorderingapp.classes.Product;

import java.util.List;

public class CartDetailActivity extends AppCompatActivity {
    private Cart cartItem;
    private Product correspondingProduct;
    private TextView quantityTextView;
    private TextView totalPriceTextView;
    private int currentQuantity;
    private int originalCartQuantity;

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
        originalCartQuantity = cartItem.getQuantity();

        // Fetch corresponding product
        fetchCorrespondingProduct();

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
                .error(R.drawable.ic_placeholder)
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
            if (correspondingProduct != null &&
                    (originalCartQuantity - currentQuantity) < correspondingProduct.getQuantity()) {
                currentQuantity++;
                updateQuantityAndTotal();
            } else {
                Toast.makeText(this, "Không đủ số lượng sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        okButton.setOnClickListener(v -> updateCartAndProduct());
    }

    private void fetchCorrespondingProduct() {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause("name = '" + cartItem.getName() + "'");

        Backendless.Data.of(Product.class).find(queryBuilder, new AsyncCallback<List<Product>>() {
            @Override
            public void handleResponse(List<Product> response) {
                if (response != null && !response.isEmpty()) {
                    correspondingProduct = response.get(0);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(CartDetailActivity.this,
                        "Error fetching product: " + fault.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuantityAndTotal() {
        quantityTextView.setText(String.valueOf(currentQuantity));
        int totalPrice = currentQuantity * cartItem.getPrice();
        totalPriceTextView.setText("Tổng tiền: " + totalPrice);
    }

    private void updateCartAndProduct() {
        if (correspondingProduct == null) {
            Toast.makeText(this, "Không tìm thấy sản phẩm tương ứng", Toast.LENGTH_SHORT).show();
            return;
        }

        // Calculate quantity difference
        int quantityDifference = originalCartQuantity - currentQuantity;

        // Update product quantity
        correspondingProduct.setQuantity(correspondingProduct.getQuantity() + quantityDifference);

        // Update product first
        Backendless.Data.of(Product.class).save(correspondingProduct, new AsyncCallback<Product>() {
            @Override
            public void handleResponse(Product response) {
                // After product is updated, update cart
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

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(CartDetailActivity.this,
                        "Error updating product: " + fault.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}