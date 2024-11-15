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

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.foodorderingapp.classes.Cart;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CheckOutActivity extends AppCompatActivity {

    private RecyclerView checkoutRecyclerView;
    private CartAdapter checkoutAdapter;
    private List<Cart> checkoutList;
    private EditText deliveryAddressEditText;
    private TextView totalPriceTextView;
    private static final String TAG = "CheckOutActivity";
    private AtomicInteger pendingDeletions;
    private boolean isProcessingOrder = false;

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
        totalPriceTextView = findViewById(R.id.total_price_checkout);

        Intent intent = getIntent();
        checkoutList = (ArrayList<Cart>) intent.getSerializableExtra("selectedCartItems");
        int totalPrice = intent.getIntExtra("totalPrice", 0);

        if (checkoutList != null) {
            checkoutAdapter = new CartAdapter(checkoutList, this, null);
            checkoutRecyclerView.setAdapter(checkoutAdapter);
            totalPriceTextView.setText("Tổng tiền: " + totalPrice);
        }

        confirmOrderButton.setOnClickListener(v -> processOrder());
    }

    private void processOrder() {
        if (isProcessingOrder) {
            return; // Prevent multiple processing
        }

        String deliveryAddress = deliveryAddressEditText.getText().toString().trim();
        if (deliveryAddress.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
            return;
        }

        isProcessingOrder = true;
        Button confirmButton = findViewById(R.id.confirm_order_button);
        confirmButton.setEnabled(false);

        pendingDeletions = new AtomicInteger(checkoutList.size());

        for (Cart item : checkoutList) {
            deleteCartItem(item);
        }
    }

    private void deleteCartItem(Cart item) {
        Backendless.Data.of(Cart.class).remove(item, new AsyncCallback<Long>() {
            @Override
            public void handleResponse(Long response) {
                if (pendingDeletions.decrementAndGet() == 0) {
                    // All items deleted successfully
                    runOnUiThread(() -> {
                        setResult(RESULT_OK);
                        finish();
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error deleting item: " + fault.getMessage());
                runOnUiThread(() -> {
                    isProcessingOrder = false;
                    findViewById(R.id.confirm_order_button).setEnabled(true);
                    Toast.makeText(CheckOutActivity.this,
                            "Lỗi khi xử lý đơn hàng: " + fault.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isProcessingOrder) {
            Toast.makeText(this, "Đang xử lý đơn hàng, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }
}