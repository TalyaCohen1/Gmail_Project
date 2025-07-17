package com.example.android_app.model;

// This class represents the response from the login API.
// It contains the user's token, full name, profile image, user ID, email address,
public class LoginResponse {
    private String token;
    private String fullName;
    private String profileImage;
    private String userId;
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
