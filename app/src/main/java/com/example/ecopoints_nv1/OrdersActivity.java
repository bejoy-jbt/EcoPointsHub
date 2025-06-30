package com.example.ecopoints_nv1;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eco_points_1a.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersActivity extends BaseActivity {
    private RecyclerView recyclerView;
    private OrdersAdapter adapter;
    private List<OrderModel> orderList;
    private FirebaseFirestore db;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_orders;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSelectedNavItem(R.id.nav_orders);

        recyclerView = findViewById(R.id.recyclerViewOrders);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        adapter = new OrdersAdapter(orderList, this);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        fetchOrders(); // Fetch orders when activity starts
    }

    private void fetchOrders() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userId = currentUser.getUid();
            Log.d("OrdersActivity", "Fetching orders for userId: " + userId);

            db.collection("orders") // Changed from "posts" to "orders"
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        orderList.clear(); // Clear old data

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            OrderModel order = document.toObject(OrderModel.class);
                            orderList.add(order);
                            Log.d("OrdersActivity", "Fetched order: " + document.getData());
                        }

                        if (orderList.isEmpty()) {
                            Toast.makeText(this, "No orders found", Toast.LENGTH_SHORT).show();
                            Log.d("OrdersActivity", "No orders found for this user.");
                        }

                        adapter.notifyDataSetChanged(); // Refresh RecyclerView
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                        Log.e("OrdersActivity", "Error fetching orders", e);
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            Log.e("OrdersActivity", "Current user is null.");
        }
    }
}
