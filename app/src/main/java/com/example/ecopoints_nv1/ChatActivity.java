package com.example.ecopoints_nv1;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.eco_points_1a.R;

public class ChatActivity extends BaseActivity {
    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_chat; // Load the chat screen inside base layout
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSelectedNavItem(R.id.nav_chat); // Highlight "Chat" in bottom navigation
    }
}