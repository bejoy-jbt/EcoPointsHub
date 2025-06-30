package com.example.ecopoints_nv1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.eco_points_1a.R;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrderViewHolder> {
    private List<OrderModel> orderList;
    private Context context;

    public OrdersAdapter(List<OrderModel> orderList, Context context) {
        this.orderList = orderList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderModel order = orderList.get(position);
        holder.orderDescription.setText(order.getDescription());
        holder.orderWasteName.setText("Waste Type: " + order.getWasteName());
        holder.orderWeight.setText("Weight: " + order.getWeight() + " kg");

        // Load image using Glide
        Glide.with(context).load(order.getImageUrl()).into(holder.orderImage);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    } 

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderDescription, orderWasteName, orderWeight;
        ImageView orderImage;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderDescription = itemView.findViewById(R.id.orderDescription);
            orderWasteName = itemView.findViewById(R.id.orderWasteName);
            orderWeight = itemView.findViewById(R.id.orderWeight);
            orderImage = itemView.findViewById(R.id.orderImage);
        }
    }
}
