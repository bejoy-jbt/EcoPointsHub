package com.example.ecopoints_nv1;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.example.eco_points_1a.R;

import java.util.HashMap;
import java.util.Map;

public class PostActivity extends BaseActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageReference storageRef;
    private FirebaseFirestore db;
    private Dialog postDialog;
    private ProgressBar progressBar;
    private String selectedCategory = "";
    private Button confirmButton;

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_post;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setSelectedNavItem(R.id.nav_post);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("uploads");

        Button addPostButton = findViewById(R.id.addPostButton);
        addPostButton.setOnClickListener(v -> showPostPopup());
    }

    private void showPostPopup() {
        postDialog = new Dialog(this);
        postDialog.setContentView(R.layout.popup_add_post);
        postDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        postDialog.setCancelable(false);

        EditText wasteName = postDialog.findViewById(R.id.editTextWasteName);
        EditText wasteWeight = postDialog.findViewById(R.id.editTextWasteWeight);
        EditText wasteDescription = postDialog.findViewById(R.id.editTextWasteDescription);
        EditText wasteLocation = postDialog.findViewById(R.id.editTextWasteLocation); // Location field
        ImageView wasteImage = postDialog.findViewById(R.id.imageView);
        Spinner categorySpinner = postDialog.findViewById(R.id.spinnerCategory);
        confirmButton = postDialog.findViewById(R.id.confirmButton);
        Button cancelButton = postDialog.findViewById(R.id.cancelButton);
        progressBar = postDialog.findViewById(R.id.progressBar);

        // Set up category spinner
        String[] categories = {"Plastic", "Paper", "Metal", "Glass", "Electronics", "Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = "";
            }
        });

        // Image selection
        wasteImage.setOnClickListener(v -> openImagePicker());

        // Real-time input validation
        wasteName.addTextChangedListener(new TextValidator(wasteName));
        wasteWeight.addTextChangedListener(new TextValidator(wasteWeight));
        wasteDescription.addTextChangedListener(new TextValidator(wasteDescription));
        wasteLocation.addTextChangedListener(new TextValidator(wasteLocation));

        // Confirm button click
        confirmButton.setOnClickListener(v -> {
            String name = wasteName.getText().toString().trim();
            String weight = wasteWeight.getText().toString().trim();
            String description = wasteDescription.getText().toString().trim();
            String location = wasteLocation.getText().toString().trim();

            // Input validation
            if (name.isEmpty() || weight.isEmpty() || description.isEmpty() || selectedCategory.isEmpty() || location.isEmpty()) {
                Toast.makeText(this, "Please fill all fields including location!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (imageUri == null) {
                Toast.makeText(this, "Please select an image!", Toast.LENGTH_SHORT).show();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);
            confirmButton.setEnabled(false); // Disable button during upload
            uploadImageToFirebase(name, weight, description, selectedCategory, location);
        });

        // Cancel button click
        cancelButton.setOnClickListener(v -> postDialog.dismiss());

        postDialog.show();
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            if (postDialog != null) {
                ImageView wasteImage = postDialog.findViewById(R.id.imageView);
                wasteImage.setImageURI(imageUri);
            }
        }
    }

    private void uploadImageToFirebase(String name, String weight, String description, String category, String location) {
        if (imageUri != null) {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            if (auth.getCurrentUser() == null) {
                Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show();
                return;
            }
            String userId = auth.getCurrentUser().getUid();
            String orderId = String.valueOf(System.currentTimeMillis());

            StorageReference fileRef = storageRef.child("waste_images/" + orderId + ".jpg");

            fileRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        progressBar.setVisibility(View.GONE);
                        confirmButton.setEnabled(true); // Enable button after upload
                        storeDataInFirestore(name, weight, description, uri.toString(), orderId, userId, category, location);
                    }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        confirmButton.setEnabled(true); // Enable button after failure
                        Toast.makeText(PostActivity.this, "Image Upload Failed! " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void storeDataInFirestore(String name, String weight, String description, String imageUrl,
                                      String orderId, String userId, String category, String location) {
        Map<String, Object> wasteData = new HashMap<>();
        wasteData.put("wasteName", name);
        wasteData.put("id", orderId);
        wasteData.put("weight", weight);
        wasteData.put("description", description);
        wasteData.put("category", category);
        wasteData.put("location", location); // Added location
        wasteData.put("imageUrl", imageUrl);
        wasteData.put("userId", userId);

        db.collection("orders").document(orderId)
                .set(wasteData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(PostActivity.this, "Post Added!", Toast.LENGTH_SHORT).show();
                    postDialog.dismiss();
                })
                .addOnFailureListener(e -> Toast.makeText(PostActivity.this, "Error saving post! " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // Real-time validation: custom TextWatcher
    private class TextValidator implements TextWatcher {
        private EditText editText;

        public TextValidator(EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            validateInput(editText);
        }

        @Override
        public void afterTextChanged(Editable editable) {}

        private void validateInput(EditText editText) {
            if (editText.getText().toString().trim().isEmpty()) {
                editText.setBackgroundResource(R.drawable.edit_text_error_border); // Assuming you've created this error border
            } else {
                editText.setBackgroundResource(R.drawable.edit_text_normal_border); // Default border
            }
        }
    }
}
