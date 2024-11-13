package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingapp.classes.Cart;

import java.util.ArrayList;
import java.util.List;

public class CheckOutActivity extends AppCompatActivity {

    private RecyclerView checkoutRecyclerView;
    private CartAdapter checkoutAdapter;
    private List<Cart> checkoutList;
    private EditText deliveryAddressEditText;
    private TextView totalPriceTextView; // Add TextView for total price

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        ImageButton backButton = findViewById(R.id.button_back_checkout);
        backButton.setOnClickListener(v -> finish());

        checkoutRecyclerView = findViewById(R.id.checkout_list);
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        deliveryAddressEditText = findViewById(R.id.delivery_address);
        Button confirmOrderButton = findViewById(R.id.confirm_order_button);
        totalPriceTextView = findViewById(R.id.total_price_checkout); // Initialize the TextView


        // Get the selected cart items and total price from the Intent
        Intent intent = getIntent();
        checkoutList = (ArrayList<Cart>) intent.getSerializableExtra("selectedCartItems");
        int totalPrice = intent.getIntExtra("totalPrice", 0);


        if (checkoutList != null) {
            checkoutAdapter = new CartAdapter(checkoutList, this, null);
            checkoutRecyclerView.setAdapter(checkoutAdapter);

            totalPriceTextView.setText("Tổng tiền: " + totalPrice);
        }


        confirmOrderButton.setOnClickListener(v -> {
            String deliveryAddress = deliveryAddressEditText.getText().toString().trim();
            if (deliveryAddress.isEmpty()) {
                Toast.makeText(CheckOutActivity.this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();

            } else {
                // TODO: Process the order
                Toast.makeText(CheckOutActivity.this, "Đơn hàng đã được xác nhận!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}