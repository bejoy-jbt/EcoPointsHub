package com.example.ecopoints_nv1;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.eco_points_1a.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    // UI Elements
    private EditText emailField, nameField, phoneField, passwordField;
    private RadioButton userRadioButton, collectorRadioButton;
    private Button continueButton, backButton, loginButton;
    private ImageView passwordToggle;  // Password visibility toggle

    // Firebase instances
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Initialize Firebase Auth and Firestore
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Link UI elements
        emailField = findViewById(R.id.emailField);
        nameField = findViewById(R.id.nameField);
        phoneField = findViewById(R.id.phoneField);
        passwordField = findViewById(R.id.passwordField);
        userRadioButton = findViewById(R.id.userRadioButton);
        collectorRadioButton = findViewById(R.id.collectorRadioButton);
        continueButton = findViewById(R.id.continueButton);
        backButton = findViewById(R.id.backButton);
        loginButton = findViewById(R.id.loginButton);
        passwordToggle = findViewById(R.id.passwordToggle); // Password toggle icon

        // Set click listener for the Continue button
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpUser();
            }
        });

        // Set click listener for the Back button
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToLogin();
            }
        });

        // Set click listener for the Login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToLogin();
            }
        });

        // Password visibility toggle logic
        passwordToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (passwordField.getInputType() == 129) {  // 129 is for password input type
                    // Show password
                    passwordField.setInputType(1);  // 1 is for plain text input type
                    passwordToggle.setImageResource(R.drawable.visibility);  // Show the eye icon
                } else {
                    // Hide password
                    passwordField.setInputType(129);  // 129 is for password input type
                    passwordToggle.setImageResource(R.drawable.visibility_off);  // Hide the eye icon
                }
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
        startActivity(intent);
        finish(); // Prevents returning to SignUpActivity
    }

    private void signUpUser() {
        // Get input values
        String email = emailField.getText().toString().trim();
        String name = nameField.getText().toString().trim();
        String phone = phoneField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Check which role is selected
        final String role;
        if (userRadioButton.isChecked()) {
            role = "User";
        } else if (collectorRadioButton.isChecked()) {
            role = "Collector";
        } else {
            role = null;
        }

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            emailField.setError("Email is required");
            emailField.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            nameField.setError("Name is required");
            nameField.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneField.setError("Phone number is required");
            phoneField.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordField.setError("Password is required");
            passwordField.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordField.setError("Password must be at least 6 characters long");
            passwordField.requestFocus();
            return;
        }

        if (role == null) {
            Toast.makeText(this, "Please select a role (User or Collector)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Authentication
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<com.google.firebase.auth.AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<com.google.firebase.auth.AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Store user data in Firestore
                            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String userId = firebaseUser.getUid();

                                // Prepare user data
                                Map<String, Object> userData = new HashMap<>();
                                userData.put("name", name);
                                userData.put("email", email);
                                userData.put("phone", phone);
                                userData.put("role", role);

                                // Save user data in Firestore
                                firestore.collection("users").document(userId)
                                        .set(userData)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(SignUpActivity.this, "Signup Successful", Toast.LENGTH_SHORT).show();
                                                    navigateToLogin();
                                                } else {
                                                    Toast.makeText(SignUpActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SignUpActivity.this, "Signup Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
