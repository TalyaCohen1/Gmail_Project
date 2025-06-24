package com.example.android_app.data.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import com.example.android_app.model.Email;
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
    @POST("api/users/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @GET("/api/emails/{id}")
    Call<Email> getEmailById(@Path("id") String id, @Header("Authorization") String userId);
}
