package com.example.foodorderingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodorderingapp.classes.Cart;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<Cart> cartList;
    private Context context;
    private OnCartClickListener listener;

    public interface OnCartClickListener {
        void onDeleteClick(int position);
        void onCheckboxClick(int position, boolean isChecked);
        void onItemClick(int position);
    }

    public CartAdapter(List<Cart> cartList, Context context, OnCartClickListener listener) {
        this.cartList = cartList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        Cart cartItem = cartList.get(position);

        // Handle image loading with null/empty check
        String imageUrl = cartItem.getImage_source();
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            // Use ic_food as default image
            holder.productImageView.setImageResource(R.drawable.ic_food);
        } else {
            // Load image from URL using Glide
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_food)  // Use ic_food as error image as well
                    .into(holder.productImageView);
        }

        // Set text values
        holder.productName.setText(cartItem.getName());
        holder.quantity.setText("Số lượng: " + cartItem.getQuantity());
        holder.total.setText("Tổng tiền: " + cartItem.getTotal_price());

        // Set tag and checkbox state
        holder.itemView.setTag(cartItem.getObjectId());
        holder.checkBox.setChecked(cartItem.isChecked());

        // Set click listener for delete button
        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });

        // Set click listener for checkbox
        holder.checkBox.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onCheckboxClick(holder.getAdapterPosition(), holder.checkBox.isChecked());
            }
        });

        // Set click listener for the whole item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                listener.onItemClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartList != null ? cartList.size() : 0;
    }

    public void updateCartList(List<Cart> newCartList) {
        this.cartList = newCartList;
        notifyDataSetChanged();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView productImageView;
        TextView productName;
        TextView quantity;
        TextView total;
        ImageButton deleteButton;
        CheckBox checkBox;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.product_image_view);
            productName = itemView.findViewById(R.id.product_name);
            quantity = itemView.findViewById(R.id.quantity);
            total = itemView.findViewById(R.id.total);
            deleteButton = itemView.findViewById(R.id.button_delete);
            checkBox = itemView.findViewById(R.id.check_box_cart);
        }
    }
}