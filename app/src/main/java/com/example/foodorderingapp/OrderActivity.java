package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.foodorderingapp.classes.Order;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrderActivity extends AppCompatActivity implements OrderAdapter.OnItemClickListener {

    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ImageButton backButton;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        recyclerViewOrders = findViewById(R.id.orderRecyclerView);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        backButton = findViewById(R.id.button_back);
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList, this);
        recyclerViewOrders.setAdapter(orderAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefreshing) {
                    loadOrders();
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        swipeRefreshLayout.setRefreshing(true);
        isRefreshing = true;
        loadOrders();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadOrders() {
        String currentUserId = Backendless.UserService.CurrentUser().getObjectId();
        String whereClause = "ownerId = '" + currentUserId + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.setSortBy("created DESC"); // Sort by created date

        Backendless.Data.of(Order.class).find(queryBuilder, new AsyncCallback<List<Order>>() {
            @Override
            public void handleResponse(List<Order> orders) {
                orderList.clear();
                orderList.addAll(orders);
                orderAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
                isRefreshing = false;
                if (orders.isEmpty()) {
                    Toast.makeText(OrderActivity.this, "Không có đơn hàng nào.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                swipeRefreshLayout.setRefreshing(false);
                isRefreshing = false;
                Toast.makeText(OrderActivity.this, "Lỗi: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("OrderActivity", "Error loading orders: " + fault.getMessage());
            }
        });
    }

    @Override
    public void onItemClick(Order order) {
        Intent intent = new Intent(OrderActivity.this, OrderDetailActivity.class);
        intent.putExtra("order", order);
        startActivity(intent);
    }
}