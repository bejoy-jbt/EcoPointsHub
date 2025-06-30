package com.example.ecopoints_nv1;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.eco_points_1a.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        setupBottomNavigation();
    }

    protected int getLayoutResourceId() {
        return R.layout.activity_home; // Default layout
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startNewActivity(HomeActivity.class);
                    return true;
                } else if (itemId == R.id.nav_orders) {
                    startNewActivity(OrdersActivity.class);
                    return true;
                } else if (itemId == R.id.nav_post) {
                    startNewActivity(PostActivity.class);
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    startNewActivity(ChatActivity.class);
                    return true;
                } else if (itemId == R.id.nav_account) {
                    startNewActivity(AccountActivity.class);
                    return true;
                }
                return false;
            }
        });
    }

    private void startNewActivity(Class<?> activityClass) {
        if (!getClass().equals(activityClass)) { // Prevent reloading the same activity
            Intent intent = new Intent(this, activityClass);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
    }

    protected void setSelectedNavItem(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);
    }
}
