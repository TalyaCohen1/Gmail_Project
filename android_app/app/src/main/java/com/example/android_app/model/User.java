package com.example.android_app.model;

import com.google.gson.annotations.SerializedName;

// User class representing a user in the application
public class User {
    private String id;
    @SerializedName("fullName")
    private String username;
    @SerializedName("profileImage")
    private String profilePicUrl;

    @SerializedName("emailAddress")
    private String emailAddress;

    public User(String id, String username, String profilePicUrl) {
        this.id = id;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getEmailAddress() {
        return emailAddress;
    }
}