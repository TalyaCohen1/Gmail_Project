package com.example.android_app.model;

import com.google.gson.annotations.SerializedName;

public class LabelCreateRequest {
    @SerializedName("name")
    private String name;

    public LabelCreateRequest(String name) {
        this.name = name;
    }

    // Getter is required by Gson for serialization, even if not explicitly used by your code
    public String getName() {
        return name;
    }
}
