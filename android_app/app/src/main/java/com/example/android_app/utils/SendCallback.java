package com.example.android_app.utils;

public interface SendCallback {
    void onSuccess();
    void onFailure(String error);
}