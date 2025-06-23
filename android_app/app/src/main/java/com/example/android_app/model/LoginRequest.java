package com.example.android_app.model;
import com.example.android_app.model.LoginResponse;

public class LoginResult {
    private LoginResponse response;
    private String error;

    public LoginResult(LoginResponse response) {
        this.response = response;
    }

    public LoginResult(String error) {
        this.error = error;
    }

    public LoginResponse getResponse() { return response; }
    public String getError() { return error; }

    public String getToken() {
        return response != null ? response.getToken() : null;
    }

    public String getFullName() {
        return response != null ? response.getFullName() : null;
    }

    public String getProfileImage() {
        return response != null ? response.getProfileImage() : null;
    }

    public String getUserId() {
        return response != null ? response.getUserId() : null;
    }
}
