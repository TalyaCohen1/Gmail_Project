package com.example.android_app.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
    private static final String PREFS_NAME = "SmailPrefs";

    public static void save(Context context, String key, String value) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(key, value).apply();
    }

    public static String get(Context context, String key) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getString(key, null);
    }

    //save user profile data
    public static void saveProfile(Context context, String fullName, String imageUrl) {
        save(context, "fullName", fullName);
        save(context, "profileImage", imageUrl);
    }

    //use to clear all data- for Logout
    public static void clearAll(Context context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
    public static String load(Context context, String key, String defaultValue) {
        SharedPreferences prefs = context.getSharedPreferences("SmailPrefs", Context.MODE_PRIVATE);
        return prefs.getString(key, defaultValue);
    }
}

