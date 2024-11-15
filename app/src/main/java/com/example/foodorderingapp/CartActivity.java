package com.example.foodorderingapp;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartClickListener {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<Cart> cartList;
    private TextView totalPriceSelectedTextView;
    private Button checkoutButton;
    private int totalPrice;
    private static final int CHECKOUT_REQUEST_CODE = 1001;
    private static final int CART_DETAIL_REQUEST_CODE = 1002;

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

        checkoutButton.setOnClickListener(v -> handleCheckout());

        calculateTotalPriceSelected();
    }

    private void retrieveCartData() {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();

        Backendless.Data.of(Cart.class).find(queryBuilder, new AsyncCallback<List<Cart>>() {
            @Override
            public void handleResponse(List<Cart> response) {
                cartList = response;
                if (cartAdapter == null) {
                    cartAdapter = new CartAdapter(cartList, CartActivity.this, CartActivity.this);
                    cartRecyclerView.setAdapter(cartAdapter);
                } else {
                    cartAdapter.updateCartList(cartList);
                }
                calculateTotalPriceSelected();
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
        this.totalPrice = 0;

        if (cartList != null) {
            for (Cart cartItem : cartList) {
                if (cartItem.isChecked()) {
                    this.totalPrice += cartItem.getTotal_price();
                }
            }
        }

        totalPriceSelectedTextView.setText("Tổng tiền: " + this.totalPrice);
    }

    private void handleCheckout() {
        List<Cart> selectedCartItems = new ArrayList<>();
        for(Cart item : cartList) {
            if(item.isChecked()) {
                selectedCartItems.add(item);
            }
        }

        if (selectedCartItems.isEmpty()) {
            Toast.makeText(CartActivity.this, "Hãy chọn món hàng cần thanh toán", Toast.LENGTH_SHORT).show();
        } else {
            Intent checkoutIntent = new Intent(CartActivity.this, CheckOutActivity.class);
            checkoutIntent.putExtra("selectedCartItems", new ArrayList<>(selectedCartItems));
            checkoutIntent.putExtra("totalPrice", totalPrice);
            startActivityForResult(checkoutIntent, CHECKOUT_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHECKOUT_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> deletedItems = data.getStringArrayListExtra("deletedItems");
            if (deletedItems != null && !deletedItems.isEmpty()) {
                // Xóa các items đã thanh toán khỏi cartList
                cartList.removeIf(cartItem ->
                        deletedItems.contains(cartItem.getObjectId())
                );
                cartAdapter.notifyDataSetChanged();
                calculateTotalPriceSelected();
            }
        }
    }

    @Override
    public void onItemClick(int position) {
        Cart selectedItem = cartList.get(position);
        Intent intent = new Intent(this, CartDetailActivity.class);
        intent.putExtra("cartItem", selectedItem);
        startActivityForResult(intent, CART_DETAIL_REQUEST_CODE);
    }
}