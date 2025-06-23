package com.example.android_app.model;

public class LoginRequest {
    private String emailAddress;
    private String password;

    public LoginRequest(String emailAddress, String password) {
        this.emailAddress = emailAddress;
        this.password = password;
    }
}