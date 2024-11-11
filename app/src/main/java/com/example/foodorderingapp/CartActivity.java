package com.example.foodorderingapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.example.foodorderingapp.classes.Cart;

import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartClickListener {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<Cart> cartList;
    private TextView totalPriceSelectedTextView;
    private Button checkoutButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        cartRecyclerView = findViewById(R.id.cart_list);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        retrieveCartData();

        totalPriceSelectedTextView = findViewById(R.id.total_price_selected);
        checkoutButton = findViewById(R.id.button_checkout);

        checkoutButton.setOnClickListener(v -> {
            Toast.makeText(CartActivity.this, "Thanh toán", Toast.LENGTH_SHORT).show(); // tạm thời
        });

        calculateTotalPriceSelected();
    }

    private void retrieveCartData() {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();

        Backendless.Data.of(Cart.class).find(queryBuilder, new AsyncCallback<List<Cart>>() {
            @Override
            public void handleResponse(List<Cart> response) {
                cartList = response;
                cartAdapter = new CartAdapter(cartList, CartActivity.this, CartActivity.this);
                cartRecyclerView.setAdapter(cartAdapter);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(CartActivity.this, "Error retrieving cart data: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CartActivity", "Error: " + fault.getMessage());
            }
        });
    }

    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < cartList.size()) {
            Cart cartItemToDelete = cartList.get(position);
            String objectId = cartItemToDelete.getObjectId();

            if (objectId != null) {
                Backendless.Data.of(Cart.class).remove(cartItemToDelete, new AsyncCallback<Long>() {
                    @Override
                    public void handleResponse(Long response) {
                        // Remove the item from the adapter's list and update the RecyclerView
                        cartList.remove(position);
                        cartAdapter.notifyItemRemoved(position);
                        Toast.makeText(CartActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(CartActivity.this, "Error deleting item: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("CartActivity", "Error deleting item: " + fault.getMessage());
                    }
                });
            } else {
                Log.e("CartActivity", "objectId is null. Cannot delete item.");
            }
        }
    }


    @Override
    public void onCheckboxClick(int position, boolean isChecked) {
        cartList.get(position).setChecked(isChecked);
        cartAdapter.notifyItemChanged(position);
        calculateTotalPriceSelected();
    }

    private void calculateTotalPriceSelected() {
        int totalPrice = 0;

        if (cartList != null) {
            for (Cart cartItem : cartList) {
                if (cartItem.isChecked()) {
                    totalPrice += cartItem.getTotal_price();
                }
            }
        }

        totalPriceSelectedTextView.setText("Tổng tiền: " + totalPrice);
    }
}