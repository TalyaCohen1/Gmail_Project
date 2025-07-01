package com.example.android_app.utils;

import com.example.android_app.model.LoginResponse;
import com.example.android_app.data.local.UserEntity;

public class UserMapper {
    public static UserEntity fromLoginResponse(LoginResponse response) {
        return new UserEntity(
                response.getUserId(),
                response.getFullName(),
                response.getProfileImage(),
                response.getEmailAddress()
        );
    }

}
