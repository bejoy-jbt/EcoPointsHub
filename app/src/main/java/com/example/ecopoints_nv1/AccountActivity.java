package com.example.ecopoints_nv1;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.eco_points_1a.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class AccountActivity extends BaseActivity {

    // UI Elements
    private CircleImageView profileImage;
    private ImageButton buttonChangePicture;
    private ImageButton buttonBack;
    private EditText editTextName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPhone;
    private Button buttonSaveChanges;

    // Firebase instances
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private FirebaseUser currentUser;

    // Image selection
    private Uri selectedImageUri;
    private String currentProfileImageUrl;

    // Original user data to detect changes
    private String originalName;
    private String originalPhone;

    // Activity Result Launcher for image picking
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_account; // Use your account layout
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSelectedNavItem(R.id.nav_account); // Highlight Account in Nav Bar

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUser = mAuth.getCurrentUser();

        // Security check - only allow logged in users
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show();
            // Redirect to login activity
            redirectToLogin();
            return;
        }

        // Set up activity result launcher for profile image
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Display the selected image
                            profileImage.setImageURI(selectedImageUri);
                        }
                    }
                }
        );

        // Initialize UI elements
        initializeUI();

        // Set click listeners
        setupClickListeners();

        // Load user data from database
        loadUserData();
    }

    private void redirectToLogin() {
        // Replace with your login activity
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void initializeUI() {
        profileImage = findViewById(R.id.profileImage);
        buttonChangePicture = findViewById(R.id.buttonChangePicture);
        buttonBack = findViewById(R.id.buttonBack);
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPhone = findViewById(R.id.editTextPhone);
        buttonSaveChanges = findViewById(R.id.buttonSaveChanges);
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> finish());

        buttonChangePicture.setOnClickListener(v -> openImagePicker());

        buttonSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void loadUserData() {
        if (currentUser != null) {
            String uid = currentUser.getUid();

            // First, load basic info from Firebase Auth
            String authEmail = currentUser.getEmail();
            if (authEmail != null) {
                editTextEmail.setText(authEmail);
            }

            // Then check for profile data in Firestore
            DocumentReference userRef = db.collection("users").document(uid);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    // Extract user data
                    originalName = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    originalPhone = documentSnapshot.getString("phone");

                    // Store original values to detect changes
                    if (originalName != null) editTextName.setText(originalName);
                    if (email != null && !email.isEmpty()) editTextEmail.setText(email);
                    if (originalPhone != null) editTextPhone.setText(originalPhone);

                    // Keep password field filled with asterisks for security
                    editTextPassword.setText("************");

                    // Load profile image if there is one (not in your current data)
                    currentProfileImageUrl = documentSnapshot.getString("profileImageUrl");
                    if (currentProfileImageUrl != null && !currentProfileImageUrl.isEmpty()) {
                        loadProfileImage(currentProfileImageUrl);
                    }
                } else {
                    // No user document exists yet, create one with basic info
                    editTextPassword.setText("************");
                    if (currentUser.getDisplayName() != null) {
                        editTextName.setText(currentUser.getDisplayName());
                    }

                    // Initialize original values
                    originalName = editTextName.getText().toString();
                    originalPhone = "";
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(AccountActivity.this,
                        "Failed to load user data: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void loadProfileImage(String imageUrl) {
        // Use Glide to load the image
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.profile_picture) // Default placeholder
                .error(R.drawable.profile_picture) // Error placeholder
                .into(profileImage);
    }

    private void saveChanges() {
        if (currentUser == null) {
            Toast.makeText(this, "No user signed in", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get input values
        String name = editTextName.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        // Validate inputs
        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get user reference
        String uid = currentUser.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        // Track if any changes were made
        boolean hasChanges = false;

        // Update Firestore user document
        if (!name.equals(originalName)) {
            userRef.update("name", name)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AccountActivity.this,
                                "Name updated successfully",
                                Toast.LENGTH_SHORT).show();
                        originalName = name;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AccountActivity.this,
                                "Failed to update name: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
            hasChanges = true;
        }

        if (!phone.equals(originalPhone)) {
            userRef.update("phone", phone)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AccountActivity.this,
                                "Phone number updated successfully",
                                Toast.LENGTH_SHORT).show();
                        originalPhone = phone;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AccountActivity.this,
                                "Failed to update phone: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
            hasChanges = true;
        }

        // Update email in Auth if changed
        if (!email.equals(currentUser.getEmail())) {
            currentUser.updateEmail(email)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AccountActivity.this,
                                "Email updated successfully",
                                Toast.LENGTH_SHORT).show();
                        // Also update email in Firestore
                        userRef.update("email", email);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AccountActivity.this,
                                "Failed to update email: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
            hasChanges = true;
        }

        // Update password if changed (not asterisks)
        if (!password.isEmpty() && !password.matches("\\*+")) {
            currentUser.updatePassword(password)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AccountActivity.this,
                                "Password updated successfully",
                                Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AccountActivity.this,
                                "Failed to update password: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
            hasChanges = true;
        }

        // If uploaded a new profile image
        if (selectedImageUri != null) {
            uploadProfileImage(uid, userRef);
            hasChanges = true;
        }

        // If no changes were made
        if (!hasChanges) {
            Toast.makeText(this, "No changes detected", Toast.LENGTH_SHORT).show();
        }
    }

    private void uploadProfileImage(String uid, DocumentReference userRef) {
        // Create a storage reference
        StorageReference storageRef = storage.getReference()
                .child("profile_images")
                .child(uid + ".jpg");

        // Upload file to Firebase Storage
        storageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the download URL
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Update profile image URL in Firestore
                        userRef.update("profileImageUrl", imageUrl)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(AccountActivity.this,
                                            "Profile picture updated successfully",
                                            Toast.LENGTH_SHORT).show();
                                    currentProfileImageUrl = imageUrl;
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(AccountActivity.this,
                                            "Failed to update profile picture URL: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AccountActivity.this,
                            "Failed to upload image: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }
}