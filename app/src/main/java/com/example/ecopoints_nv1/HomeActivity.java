package com.example.ecopoints_nv1;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.eco_points_1a.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private TextView textViewUserName;
    private TextView locationText;
    private Button btnSortTrash, btnClaim;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private BottomNavigationView bottomNavigationView;
    private ImageView gifImageView;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private ListenerRegistration userListener; // Listener for real-time updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        gifImageView = findViewById(R.id.lottieAnimationView);

        // Initialize UI elements
        textViewUserName = findViewById(R.id.textViewUserName);
        btnSortTrash = findViewById(R.id.btnSortTrash);
        btnClaim = findViewById(R.id.btnClaim);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        locationText = findViewById(R.id.locationText);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Request location permissions
        requestLocationPermission();

        // Set up real-time listener for user data
        setupUserListener();

        // Adjust window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bottom Navigation Click Listener
        bottomNavigationView.setOnItemSelectedListener(new BottomNavigationView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true; // Stay on HomeActivity
                } else if (itemId == R.id.nav_orders) {
                    startActivity(new Intent(HomeActivity.this, OrdersActivity.class));
                    return true;
                } else if (itemId == R.id.nav_post) {
                    startActivity(new Intent(HomeActivity.this, PostActivity.class));
                    return true;
                } else if (itemId == R.id.nav_chat) {
                    startActivity(new Intent(HomeActivity.this, ChatActivity.class));
                    return true;
                } else if (itemId == R.id.nav_account) {
                    startActivity(new Intent(HomeActivity.this, AccountActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    private void setupUserListener() {
        String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        if (userId == null) {
            textViewUserName.setText(" ");
            return;
        }

        DocumentReference userRef = db.collection("users").document(userId);

        // Add real-time listener to the user document
        userListener = userRef.addSnapshotListener((document, error) -> {
            if (error != null) {
                textViewUserName.setText("Hi, User!");
                return;
            }

            if (document != null && document.exists()) {
                String userName = document.getString("name");
                String userRole = document.getString("role");

                textViewUserName.setText("Hi, " + userName + "!");

                if ("User".equals(userRole)) {
                    btnSortTrash.setVisibility(View.VISIBLE);
                    btnClaim.setVisibility(View.GONE);

                    if (gifImageView != null) {
                        gifImageView.setVisibility(View.VISIBLE);
                    }

                    bottomNavigationView.setVisibility(View.VISIBLE);
                } else if ("Collector".equals(userRole)) {
                    btnSortTrash.setVisibility(View.GONE);
                    btnClaim.setVisibility(View.VISIBLE);

                    if (gifImageView != null) {
                        gifImageView.setVisibility(View.GONE);
                    }

                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            } else {
                textViewUserName.setText("Hi, User!");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh location when returning to the activity
        getLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove the snapshot listener when the activity is destroyed
        if (userListener != null) {
            userListener.remove();
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                locationText.setText("Location unavailable");
            }
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                try {
                    List<Address> addresses = geocoder.getFromLocation(
                            location.getLatitude(), location.getLongitude(), 1);

                    if (addresses != null && addresses.size() > 0) {
                        String cityName = addresses.get(0).getLocality(); // City name

                        // If city name is null, try other location attributes
                        if (cityName == null) {
                            cityName = addresses.get(0).getSubAdminArea(); // District
                        }
                        if (cityName == null) {
                            cityName = addresses.get(0).getAdminArea(); // State
                        }

                        if (cityName != null) {
                            locationText.setText(cityName);
                        } else {
                            locationText.setText("Unknown location");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    locationText.setText("Location error");
                }
            } else {
                locationText.setText("Finding location...");
            }
        });
    }
}