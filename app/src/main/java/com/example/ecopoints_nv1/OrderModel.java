package com.example.ecopoints_nv1;

public class OrderModel {
    private String id;
    private String userId;
    private String description;
    private String imageUrl;
    private String wasteName;
    private String weight;

    // Required empty constructor for Firebase
    public OrderModel() {}

    public OrderModel(String id, String userId, String description, String imageUrl, String wasteName, String weight) {
        this.id = id;
        this.userId = userId;
        this.description = description;
        this.imageUrl = imageUrl;
        this.wasteName = wasteName;
        this.weight = weight;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getWasteName() {
        return wasteName;
    }

    public String getWeight() {
        return weight;
    }
}
