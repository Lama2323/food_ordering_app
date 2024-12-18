package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodorderingapp.classes.Order;
import com.example.foodorderingapp.classes.OrderItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OrderDetailActivity extends AppCompatActivity {

    private TextView tvPhoneNumber, tvAddress, tvPaymentMethod, tvTotal;
    private RecyclerView recyclerViewOrderItems;
    private OrderItemAdapter orderItemAdapter;
    private List<OrderItem> orderItemList;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        tvPhoneNumber = findViewById(R.id.tvPhoneNumber);
        tvAddress = findViewById(R.id.tvAddress);
        tvPaymentMethod = findViewById(R.id.tvPaymentMethod);
        tvTotal = findViewById(R.id.tvTotal);
        recyclerViewOrderItems = findViewById(R.id.recyclerViewOrderItems);
        recyclerViewOrderItems.setLayoutManager(new LinearLayoutManager(this));
        backButton = findViewById(R.id.button_back);
        orderItemList = new ArrayList<>();
        orderItemAdapter = new OrderItemAdapter(this, orderItemList);
        recyclerViewOrderItems.setAdapter(orderItemAdapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Intent intent = getIntent();
        if (intent.hasExtra("order")) {
            Order order = (Order) intent.getSerializableExtra("order");
            displayOrderDetails(order);
        }
    }

    private void displayOrderDetails(Order order) {
        tvPhoneNumber.setText(order.getPhone_number());
        tvAddress.setText(order.getAddress());
        tvPaymentMethod.setText(order.getPayment_method() == 1 ? "Tiền mặt" : "Chuyển khoản");

        try {
            JSONArray foodListArray = new JSONArray(order.getFood_list());
            for (int i = 0; i < foodListArray.length(); i++) {
                JSONObject foodObject = foodListArray.getJSONObject(i);
                String name = foodObject.getString("name");
                int quantity = foodObject.getInt("quantity");
                int price = foodObject.getInt("price");
                orderItemList.add(new OrderItem(name, quantity, price));
            }
            orderItemAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e("OrderDetailActivity", "Error parsing food list", e);
        }

        tvTotal.setText(String.format("Tổng tiền: %,d đ", order.getTotal()));
    }
}