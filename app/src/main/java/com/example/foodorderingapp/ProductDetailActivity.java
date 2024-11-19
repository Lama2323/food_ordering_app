package com.example.foodorderingapp;

import android.os.Bundle;
import android.view.View;
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
import com.example.foodorderingapp.classes.Product;

public class ProductDetailActivity extends AppCompatActivity {
    private ImageView productImage;
    private TextView nameTextView;
    private TextView priceTextView;
    private TextView descriptionTextView;
    private ImageButton btnDecrease;
    private ImageButton btnIncrease;
    private TextView tvQuantity;
    private Button btnAddToCart;

    private Product currentProduct;
    private int selectedQuantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize views
        productImage = findViewById(R.id.productDetailImage);
        nameTextView = findViewById(R.id.productDetailName);
        priceTextView = findViewById(R.id.productDetailPrice);
        descriptionTextView = findViewById(R.id.productDetailDescription);
        btnDecrease = findViewById(R.id.btnDecrease);
        btnIncrease = findViewById(R.id.btnIncrease);
        tvQuantity = findViewById(R.id.tvQuantity);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        // Set up quantity controls
        btnDecrease.setOnClickListener(v -> updateQuantity(-1));
        btnIncrease.setOnClickListener(v -> updateQuantity(1));

        String objectId = getIntent().getStringExtra("objectId");

        if (objectId != null) {
            loadProduct(objectId);
        }

        // Set up Add to Cart button
        btnAddToCart.setOnClickListener(v -> addToCart());
    }

    private void loadProduct(String objectId) {
        Backendless.Data.of(Product.class).findById(objectId, new AsyncCallback<Product>() {
            @Override
            public void handleResponse(Product product) {
                currentProduct = product;

                // Load image
                Glide.with(ProductDetailActivity.this)
                        .load(product.getImage_source())
                        .placeholder(R.drawable.ic_food)
                        .into(productImage);

                // Set texts
                nameTextView.setText(product.getName());

                if (product.getQuantity() == 0) {
                    priceTextView.setText("Đã hết");
                    disableCartControls();
                } else {
                    priceTextView.setText(String.format("%,d đ", product.getPrice()));
                    enableCartControls();
                }

                descriptionTextView.setText(product.getDescription());
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ProductDetailActivity.this,
                        "Error: " + fault.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQuantity(int change) {
        int newQuantity = selectedQuantity + change;
        if (newQuantity >= 1 && currentProduct != null && newQuantity <= currentProduct.getQuantity()) {
            selectedQuantity = newQuantity;
            tvQuantity.setText(String.valueOf(selectedQuantity));
        }
    }

    private void addToCart() {
        if (currentProduct == null) return;

        // Disable the add to cart button to prevent double-clicking
        btnAddToCart.setEnabled(false);

        // Create cart item
        Cart cartItem = new Cart(
                currentProduct.getImage_source(),
                currentProduct.getName(),
                currentProduct.getPrice(),
                selectedQuantity
        );

        // Update product quantity in Backendless
        int newQuantity = currentProduct.getQuantity() - selectedQuantity;
        currentProduct.setQuantity(newQuantity);

        // First update the product quantity
        Backendless.Data.of(Product.class).save(currentProduct, new AsyncCallback<Product>() {
            @Override
            public void handleResponse(Product updatedProduct) {
                // After successful product update, add to cart
                Cart.addToCart(cartItem);

                // Update UI
                if (newQuantity == 0) {
                    priceTextView.setText("Đã hết");
                    disableCartControls();
                } else {
                    enableCartControls();
                    // Reset selected quantity
                    selectedQuantity = 1;
                    tvQuantity.setText("1");
                }

                Toast.makeText(ProductDetailActivity.this,
                        "Đã thêm vào giỏ hàng",
                        Toast.LENGTH_SHORT).show();

                // Re-enable the add to cart button
                btnAddToCart.setEnabled(true);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ProductDetailActivity.this,
                        "Lỗi: " + fault.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // Re-enable the add to cart button
                btnAddToCart.setEnabled(true);
            }
        });
    }

    private void disableCartControls() {
        btnDecrease.setEnabled(false);
        btnIncrease.setEnabled(false);
        btnAddToCart.setEnabled(false);
        tvQuantity.setText("0");
    }

    private void enableCartControls() {
        btnDecrease.setEnabled(true);
        btnIncrease.setEnabled(true);
        btnAddToCart.setEnabled(true);
        tvQuantity.setText("1");
    }
}