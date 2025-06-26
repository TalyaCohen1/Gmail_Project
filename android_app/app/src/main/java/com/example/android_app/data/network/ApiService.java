package com.example.android_app.data.network;

import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest;
import com.example.android_app.model.LoginRequest;
import com.example.android_app.model.LoginResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.PATCH;


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

    @Multipart
    @PATCH("api/users/{id}")
    Call<LoginResponse> updateUser(
            @Path("id") String userId,
            @Part("fullName") RequestBody fullName,
            @Part MultipartBody.Part profileImage
    );


    @GET("api/mails")
    Call<List<Email>> getInboxEmails(
            @Header("Authorization") String token
    );

    @GET("api/mails/{id}")
    Call<Email> getEmailDetails(
            @Header("Authorization") String token,
            @Path("id") String emailId
    );
}
