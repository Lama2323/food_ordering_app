package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserTokenStorageFactory;
import com.example.foodorderingapp.classes.Cart;
import com.example.foodorderingapp.classes.Product;

import java.util.ArrayList;
import java.util.List;

public class CartActivity extends BaseNetworkActivity implements CartAdapter.OnCartClickListener {

    private RecyclerView cartRecyclerView;
    private CartAdapter cartAdapter;
    private List<Cart> cartList;
    private TextView totalPriceSelectedTextView;
    private Button checkoutButton;
    private int totalPrice;
    private static final int CHECKOUT_REQUEST_CODE = 1001;
    private static final int CART_DETAIL_REQUEST_CODE = 1002;
    private String currentUserId;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView emptyCartText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        currentUserId = Backendless.UserService.CurrentUser().getObjectId();

        ImageButton backButton = findViewById(R.id.button_back);
        backButton.setOnClickListener(v -> finish());

        cartRecyclerView = findViewById(R.id.cart_list);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        setupSwipeRefresh();

        retrieveCartData();

        totalPriceSelectedTextView = findViewById(R.id.total_price_selected);
        checkoutButton = findViewById(R.id.button_checkout);
        emptyCartText = findViewById(R.id.empty_cart_text);

        checkoutButton.setOnClickListener(v -> handleCheckout());

        calculateTotalPriceSelected();
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        swipeRefreshLayout.setOnRefreshListener(this::retrieveCartData);
    }

    private void retrieveCartData() {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        String whereClause = "customer_id = '" + currentUserId + "'";
        queryBuilder.setWhereClause(whereClause);

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

                emptyCartText.setVisibility(cartList.isEmpty() ? View.VISIBLE : View.GONE);
                cartRecyclerView.setVisibility(cartList.isEmpty() ? View.GONE : View.VISIBLE);

                calculateTotalPriceSelected();
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(CartActivity.this, "Error retrieving cart data: " + fault.getMessage(),
                        Toast.LENGTH_SHORT).show();
                Log.e("CartActivity", "Error: " + fault.getMessage());
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onDeleteClick(int position) {
        if (position >= 0 && position < cartList.size()) {
            Cart cartItemToDelete = cartList.get(position);
            String objectId = cartItemToDelete.getObjectId();

            if (objectId != null) {

                DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                queryBuilder.setWhereClause("name = '" + cartItemToDelete.getName() + "'");

                Backendless.Data.of(Product.class).find(queryBuilder, new AsyncCallback<List<Product>>() {
                    @Override
                    public void handleResponse(List<Product> response) {
                        if (response != null && !response.isEmpty()) {
                            Product product = response.get(0);
                            product.setQuantity(product.getQuantity() + cartItemToDelete.getQuantity());

                            Backendless.Data.of(Product.class).save(product, new AsyncCallback<Product>() {
                                @Override
                                public void handleResponse(Product response) {
                                    // After product is updated, delete cart item
                                    Backendless.Data.of(Cart.class).remove(cartItemToDelete, new AsyncCallback<Long>() {
                                        @Override
                                        public void handleResponse(Long response) {
                                            cartList.remove(position);
                                            cartAdapter.notifyItemRemoved(position);
                                            calculateTotalPriceSelected();
                                            Toast.makeText(CartActivity.this, "Item deleted", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void handleFault(BackendlessFault fault) {
                                            Toast.makeText(CartActivity.this, "Error deleting item: " + fault.getMessage(),
                                                    Toast.LENGTH_SHORT).show();
                                            Log.e("CartActivity", "Error deleting item: " + fault.getMessage());
                                        }
                                    });
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Toast.makeText(CartActivity.this, "Error updating product: " + fault.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    Log.e("CartActivity", "Error updating product: " + fault.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(CartActivity.this, "Error fetching product: " + fault.getMessage(),
                                Toast.LENGTH_SHORT).show();
                        Log.e("CartActivity", "Error fetching product: " + fault.getMessage());
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
        for (Cart item : cartList) {
            if (item.isChecked()) {
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
                cartList.removeIf(cartItem -> deletedItems.contains(cartItem.getObjectId()));
                cartAdapter.notifyDataSetChanged();
                calculateTotalPriceSelected();
            }
        }
        else if (requestCode == CART_DETAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            Cart updatedCartItem = (Cart) data.getSerializableExtra("updatedCartItem");
            if (updatedCartItem != null) {
                // Tìm và cập nhật item trong cartList
                for (int i = 0; i < cartList.size(); i++) {
                    if (cartList.get(i).getObjectId().equals(updatedCartItem.getObjectId())) {
                        cartList.set(i, updatedCartItem);
                        cartAdapter.notifyItemChanged(i);
                        calculateTotalPriceSelected();
                        break;
                    }
                }
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