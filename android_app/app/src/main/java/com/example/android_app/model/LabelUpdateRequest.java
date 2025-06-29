package com.example.android_app.model;

import com.google.gson.annotations.SerializedName;

public class LabelUpdateRequest {
    @SerializedName("name")
    private String name;

    public LabelUpdateRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
