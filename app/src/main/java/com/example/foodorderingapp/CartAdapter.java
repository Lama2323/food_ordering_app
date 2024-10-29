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

        Glide.with(context)
                .load(cartItem.getImage_source())
                .placeholder(R.drawable.placeholder) // Placeholder image if loading fails
                .error(R.drawable.error) // Error image if loading fails
                .into(holder.productImageView);

        holder.productName.setText(cartItem.getName());
        holder.quantity.setText("Số lượng: " + cartItem.getQuantity());
        holder.total.setText("Tổng tiền: " + cartItem.getTotal_price());
        holder.itemView.setTag(cartItem.getObjectId());

        holder.deleteButton.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDeleteClick(holder.getAdapterPosition());
            }
        });

        holder.checkBox.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCheckboxClick(holder.getAdapterPosition(), holder.checkBox.isChecked());
            }
        });

        holder.checkBox.setChecked(cartItem.isChecked());
    }

    @Override
    public int getItemCount() {
        return cartList.size();
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