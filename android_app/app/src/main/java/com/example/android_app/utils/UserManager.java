package com.example.android_app.utils;
import android.content.Context;
import android.content.SharedPreferences;

public class UserManager {
    private static final String PREFS_NAME = "SmailPrefs";
    private static final String KEY_PROFILE_IMAGE = "profileImage";
    private static final String KEY_FULL_NAME = "fullName";

    //save username data
    public static void saveFullName(Context context, String fullName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_FULL_NAME, fullName).apply();
    }

    //save user profile image
    public static void saveProfileImage(Context context, String imageUrl) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PROFILE_IMAGE, imageUrl).apply();
    }

    // get user- fullName
    public static String getFullName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_FULL_NAME, "User");
    }

    // get the URL address of the user profile image
    public static String getProfileImage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PROFILE_IMAGE, null);
    }
}
