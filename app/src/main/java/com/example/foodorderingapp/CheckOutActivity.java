package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.foodorderingapp.classes.Cart;
import com.example.foodorderingapp.classes.Order;

import java.util.ArrayList;
import java.util.List;

public class CheckOutActivity extends AppCompatActivity {
    private RecyclerView checkoutRecyclerView;
    private CartAdapter checkoutAdapter;
    private List<Cart> checkoutList;
    private EditText deliveryAddressEditText;
    private EditText deliveryPhoneEditText;
    private TextView totalPriceTextView;
    private Button confirmOrderButton;
    private ProgressBar progressBar;
    private static final String TAG = "CheckOutActivity";
    private boolean isProcessingOrder = false;
    private int totalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_out);

        initializeViews();
        setupRecyclerView();
        loadCheckoutData();
        setupListeners();
    }

    private void initializeViews() {
        ImageButton backButton = findViewById(R.id.button_back_checkout);
        checkoutRecyclerView = findViewById(R.id.checkout_list);
        deliveryAddressEditText = findViewById(R.id.delivery_address);
        deliveryPhoneEditText = findViewById(R.id.delivery_phone_number);
        confirmOrderButton = findViewById(R.id.confirm_order_button);
        totalPriceTextView = findViewById(R.id.total_price_checkout);
        progressBar = findViewById(R.id.progressBar); // Thêm ProgressBar vào layout

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void setupRecyclerView() {
        checkoutRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        checkoutRecyclerView.setHasFixedSize(true);
    }

    private void loadCheckoutData() {
        Intent intent = getIntent();
        checkoutList = (ArrayList<Cart>) intent.getSerializableExtra("selectedCartItems");
        totalPrice = intent.getIntExtra("totalPrice", 0);

        if (checkoutList != null && !checkoutList.isEmpty()) {
            checkoutAdapter = new CartAdapter(checkoutList, this, null);
            checkoutRecyclerView.setAdapter(checkoutAdapter);
            totalPriceTextView.setText("Tổng tiền: " + totalPrice);
        } else {
            Toast.makeText(this, "Không có sản phẩm trong giỏ hàng", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupListeners() {
        findViewById(R.id.button_back_checkout).setOnClickListener(v -> onBackPressed());
        confirmOrderButton.setOnClickListener(v -> validateAndProcessOrder());
    }

    private void validateAndProcessOrder() {
        if (isProcessingOrder) {
            Toast.makeText(this, "Đơn hàng đang được xử lý, vui lòng đợi...", Toast.LENGTH_SHORT).show();
            return;
        }

        String deliveryAddress = deliveryAddressEditText.getText().toString().trim();
        String phoneNumber = deliveryPhoneEditText.getText().toString().trim();

        if (!validateOrderInput(deliveryAddress, phoneNumber)) {
            return;
        }

        startOrderProcessing(deliveryAddress, phoneNumber);
    }

    private boolean validateOrderInput(String address, String phone) {
        if (address.isEmpty()) {
            deliveryAddressEditText.setError("Vui lòng nhập địa chỉ giao hàng");
            deliveryAddressEditText.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            deliveryPhoneEditText.setError("Vui lòng nhập số điện thoại");
            deliveryPhoneEditText.requestFocus();
            return false;
        }

        if (!phone.matches("^[0-9]{10}$")) {
            deliveryPhoneEditText.setError("Số điện thoại không hợp lệ");
            deliveryPhoneEditText.requestFocus();
            return false;
        }

        return true;
    }

    private void startOrderProcessing(String address, String phone) {
        isProcessingOrder = true;
        showLoadingState(true);

        // Tạo đối tượng Order mới
        Order order = new Order(address, checkoutList, phone, totalPrice);

        // Bước 1: Lưu Order
        Backendless.Data.of(Order.class).save(order, new AsyncCallback<Order>() {
            @Override
            public void handleResponse(Order savedOrder) {
                Log.d(TAG, "Order saved successfully with ID: " + savedOrder.getObjectId());
                // Sau khi lưu Order thành công, tiến hành xóa cart items
                deleteCartItems();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error saving order: " + fault.getMessage());
                handleOrderError("Lỗi khi tạo đơn hàng: " + fault.getMessage());
            }
        });
    }

    private void deleteCartItems() {
        // Tạo danh sách objectId của các cart items cần xóa
        ArrayList<String> objectIds = new ArrayList<>();
        for (Cart cart : checkoutList) {
            if (cart.getObjectId() != null) {
                objectIds.add("'" + cart.getObjectId() + "'");
            }
        }

        if (objectIds.isEmpty()) {
            handleOrderError("Không tìm thấy sản phẩm để xóa");
            return;
        }

        // Tạo câu lệnh WHERE để xóa nhiều items cùng lúc
        String whereClause = "objectId IN (" + String.join(",", objectIds) + ")";

        Backendless.Data.of(Cart.class).remove(whereClause, new AsyncCallback<Integer>() {
            @Override
            public void handleResponse(Integer response) {
                if (response > 0) {
                    Log.d(TAG, "Successfully deleted " + response + " cart items");
                    completeOrderProcess();
                } else {
                    handleOrderError("Không thể xóa sản phẩm khỏi giỏ hàng");
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Error deleting cart items: " + fault.getMessage());
                handleOrderError("Lỗi khi xóa sản phẩm khỏi giỏ hàng");
            }
        });
    }

    private void completeOrderProcess() {
        runOnUiThread(() -> {
            // Tạo Intent để trả về kết quả
            Intent resultIntent = new Intent();
            ArrayList<String> deletedItemIds = new ArrayList<>();
            for (Cart cart : checkoutList) {
                deletedItemIds.add(cart.getObjectId());
            }
            resultIntent.putStringArrayListExtra("deletedItems", deletedItemIds);

            // Đặt kết quả và kết thúc activity
            setResult(RESULT_OK, resultIntent);

            // Hiển thị thông báo thành công
            Toast.makeText(this, "Đặt hàng thành công!", Toast.LENGTH_SHORT).show();

            // Ẩn loading và kết thúc activity
            showLoadingState(false);
            finish();
        });
    }

    private void handleOrderError(String errorMessage) {
        runOnUiThread(() -> {
            isProcessingOrder = false;
            showLoadingState(false);
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        });
    }

    private void showLoadingState(boolean isLoading) {
        if (progressBar != null) {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        confirmOrderButton.setEnabled(!isLoading);
        deliveryAddressEditText.setEnabled(!isLoading);
        deliveryPhoneEditText.setEnabled(!isLoading);
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