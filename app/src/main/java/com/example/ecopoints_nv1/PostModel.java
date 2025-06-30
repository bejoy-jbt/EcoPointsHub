package com.example.ecopoints_nv1;

import com.google.firebase.Timestamp;

public class PostModel {
    private String id;

    private String userId; // User ID of the person who posted
    private String imageUrl;
    private String description;
    private String status; // "Pending", "Accepted", "Rejected"
    private Timestamp timestamp; // Firestore timestamp for sorting

    // 🔹 Empty Constructor (Required for Firebase)
    public PostModel() { }

    // 🔹 Constructor with Parameters
    public PostModel(String id, String userId, String imageUrl, String description, String status, Timestamp timestamp) {
        this.id = id;
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.description = description;
        this.status = status;
        this.timestamp = timestamp;
    }

    // 🔹 Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }


    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
}
