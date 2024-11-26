package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.foodorderingapp.classes.Product;
import com.example.foodorderingapp.utils.VietnameseUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private List<Product> productList;
    private List<Product> allProductsList;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton cartFab;
    private EditText searchEditText;
    private TextView userNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        searchEditText = findViewById(R.id.searchEditText);
        cartFab = findViewById(R.id.cartFab);
        productList = new ArrayList<>();
        allProductsList = new ArrayList<>();
        userNameTextView = findViewById(R.id.userNameTextView);


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

        cartFab.setOnClickListener(v -> {
            Intent intent = new Intent(ProductActivity.this, CartActivity.class);
            startActivity(intent);
        });

        userNameTextView.setText((String) Backendless.UserService.CurrentUser().getProperty("name"));

        // Set up SwipeRefreshLayout
        setupSwipeRefresh();

        // Initial load of products
        loadProducts();

        // Set up search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
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
                allProductsList.clear(); // Also clear the allProductsList
                productList.addAll(response);
                allProductsList.addAll(response); // Update allProductsList
                adapter.notifyDataSetChanged();
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

    private void filterProducts(String searchText) {
        productList.clear();
        if (searchText.isEmpty()) {
            productList.addAll(allProductsList);
        } else {
            String lowerCaseSearch = VietnameseUtils.removeAccents(searchText.toLowerCase(Locale.getDefault()));
            for (Product product : allProductsList) {
                String normalizedProductName = VietnameseUtils.removeAccents(
                        product.getName().toLowerCase(Locale.getDefault())
                );
                if (normalizedProductName.contains(lowerCaseSearch)) {
                    productList.add(product);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}