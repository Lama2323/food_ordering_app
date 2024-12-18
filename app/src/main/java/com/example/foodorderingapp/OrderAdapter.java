package com.example.foodorderingapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.foodorderingapp.classes.Order;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private Context context;
    private List<Order> orderList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, OnItemClickListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.order_item, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.tvPhoneNumber.setText(order.getPhone_number());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        Backendless.Persistence.of(Order.class).findById(order.getObjectId(), new AsyncCallback<Order>() {
            @Override
            public void handleResponse(Order response) {
                Date createdDate = response.getCreated();
                holder.tvCreatedDate.setText(formatter.format(createdDate));

                boolean isDone = response.is_done(); // Assuming is_done is a boolean field
                holder.tvStatus.setText(isDone ? "Đã giao" : "Chưa giao");
                holder.tvStatus.setTextColor(isDone ? ContextCompat.getColor(context, R.color.green) : ContextCompat.getColor(context, R.color.red));
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // Handle the fault, maybe set default values
                holder.tvCreatedDate.setText("N/A");
                holder.tvStatus.setText("Unknown");
                holder.tvStatus.setTextColor(Color.GRAY);
            }
        });
        holder.tvTotal.setText(String.format("%,d đ", order.getTotal()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvPhoneNumber, tvCreatedDate, tvTotal, tvStatus;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPhoneNumber = itemView.findViewById(R.id.tvPhoneNumber);
            tvCreatedDate = itemView.findViewById(R.id.tvCreatedDate);
            tvTotal = itemView.findViewById(R.id.tvTotal);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}