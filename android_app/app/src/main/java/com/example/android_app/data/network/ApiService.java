package com.example.android_app.data.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;


import com.example.android_app.model.EmailRequest;
import com.example.android_app.model.LoginResponse;
import com.example.android_app.model.LoginRequest;

public interface ApiService {

    @Multipart
    @POST("api/users")
    Call<ResponseBody> registerUser(
            @Part("fullName") RequestBody fullName,
            @Part("emailAddress") RequestBody email,
            @Part("birthDate") RequestBody birthDate,
            @Part("gender") RequestBody gender,
            @Part("password") RequestBody password,
            @Part MultipartBody.Part profileImage
    );
    @POST("api/tokens")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @POST("api/mails")
    Call<Void> sendEmail(
            @Header("Authorization") String token,
            @Body EmailRequest email
    );
}
