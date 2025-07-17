package com.example.android_app.model;

import com.google.gson.annotations.SerializedName;
public class LoginResponse {
    @SerializedName("token")
    private String token;
    @SerializedName("fullName")
    private String fullName;
    @SerializedName("profileImage")
    private String profileImage;
    @SerializedName("_id")
    private String userId;
    @SerializedName("emailAddress")
    private String emailAddress;
    private String errorMessage;

    // Getters & Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
