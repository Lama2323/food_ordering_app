package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.foodorderingapp.classes.Product;

import java.util.ArrayList;
import java.util.List;

public class ProductActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        productList = new ArrayList<>();

        // Set up RecyclerView with Grid Layout
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new ProductAdapter(this, productList, product -> {
            // Handle item click
            Intent intent = new Intent(ProductActivity.this, ProductDetailActivity.class);
            intent.putExtra("objectId", product.getObjectId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);

        // Set up SwipeRefreshLayout
        setupSwipeRefresh();

        // Initial load of products
        loadProducts();
    }

    private void setupSwipeRefresh() {
        // Set refresh indicator colors
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        // Set up refresh listener
        swipeRefreshLayout.setOnRefreshListener(this::loadProducts);
    }

    private void loadProducts() {
        Backendless.Data.of(Product.class).find(new AsyncCallback<List<Product>>() {
            @Override
            public void handleResponse(List<Product> response) {
                productList.clear();
                productList.addAll(response);
                adapter.notifyDataSetChanged();
                // Hide refresh indicator
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(ProductActivity.this,
                        "Error: " + fault.getMessage(),
                        Toast.LENGTH_SHORT).show();
                // Hide refresh indicator even if there's an error
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}