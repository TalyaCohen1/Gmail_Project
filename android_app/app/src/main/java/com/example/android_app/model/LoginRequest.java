package com.example.android_app.model;

// This class represents the request for user login.
// It contains the user's email address and password.
public class LoginRequest {
    private String emailAddress;
    private String password;

    public LoginRequest(String emailAddress, String password) {
        this.emailAddress = emailAddress;
        this.password = password;
    }
}