package com.example.android_app.model;

import com.google.gson.annotations.SerializedName;

public class Label {
    @SerializedName("id") // Ensure this matches your backend's ID field name (e.g., "_id" if your backend uses MongoDB IDs)
    private String id;
    @SerializedName("name")
    private String name;

    public Label(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor for creating new labels (id might be null initially before backend assigns one)
    public Label(String name) {
        this.name = name;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    
    public void setName(String name) {
        this.name = name;
    }


}
