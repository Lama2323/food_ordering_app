package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.foodorderingapp.classes.Favorite;
import com.example.foodorderingapp.classes.Product;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener {

    private RecyclerView recyclerViewFavorites;
    private ProductAdapter productAdapter;
    private List<Product> favoriteProducts;
    private ImageButton backButton;
    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        recyclerViewFavorites = findViewById(R.id.favoriteRecyclerView);
        recyclerViewFavorites.setLayoutManager(new GridLayoutManager(this, 2));
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        favoriteProducts = new ArrayList<>();
        productAdapter = new ProductAdapter(this, favoriteProducts, this);
        recyclerViewFavorites.setAdapter(productAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isRefreshing) {
                    loadFavoriteProducts();
                }
            }
        });
        swipeRefreshLayout.setColorSchemeResources( // setup màu sắc của swipe refresh indicator
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );
        // Hiển thị vòng xoay khi mới vào
        swipeRefreshLayout.setRefreshing(true);
        isRefreshing = true;
        loadFavoriteProducts();

        backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void loadFavoriteProducts() {
        //progressBar.setVisibility(View.VISIBLE); // Không cần thiết vì đã dùng swipeRefreshLayout
        String currentUserId = Backendless.UserService.CurrentUser().getObjectId();
        String whereClause = "customer_id = '" + currentUserId + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.setGroupBy("product_id");

        Backendless.Data.of(Favorite.class).find(queryBuilder, new AsyncCallback<List<Favorite>>() {
            @Override
            public void handleResponse(List<Favorite> favorites) {
                if (favorites.isEmpty()) {
                    //progressBar.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false); // Ẩn swipe refresh
                    isRefreshing = false;
                    Toast.makeText(FavoriteActivity.this, "Không có sản phẩm yêu thích nào.", Toast.LENGTH_SHORT).show();
                    return;
                }

                List<String> productIds = new ArrayList<>();
                for (Favorite favorite : favorites) {
                    productIds.add(favorite.getProduct_id());
                }

                loadProductsByIds(productIds);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                //progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false); // Ẩn swipe refresh
                isRefreshing = false;
                Toast.makeText(FavoriteActivity.this, "Lỗi: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FavoriteActivity", "Error loading favorites: " + fault.getMessage());
            }
        });
    }

    private void loadProductsByIds(List<String> productIds) {
        String whereClause = "objectId in ('" + String.join("', '", productIds) + "')";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of(Product.class).find(queryBuilder, new AsyncCallback<List<Product>>() {
            @Override
            public void handleResponse(List<Product> products) {
                favoriteProducts.clear();
                favoriteProducts.addAll(products);
                productAdapter.notifyDataSetChanged();
                //progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false); // Ẩn swipe refresh
                isRefreshing = false;
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                //progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false); // Ẩn swipe refresh
                isRefreshing = false;
                Toast.makeText(FavoriteActivity.this, "Lỗi: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("FavoriteActivity", "Error loading products: " + fault.getMessage());
            }
        });
    }

    @Override
    public void onItemClick(Product product) {
        Intent intent = new Intent(FavoriteActivity.this, ProductDetailActivity.class);
        intent.putExtra("objectId", product.getObjectId());
        startActivity(intent);
    }
}