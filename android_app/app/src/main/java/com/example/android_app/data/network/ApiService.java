package com.example.android_app.data.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH; // For partial updates
import retrofit2.http.Part;
import retrofit2.http.HTTP; // For DELETE with a body


import com.example.android_app.model.EmailRequest;
import com.example.android_app.model.LoginResponse;
import com.example.android_app.model.LoginRequest;
import com.example.android_app.model.Label;
import com.example.android_app.model.Mail;
import com.example.android_app.model.LabelCreateRequest;
import com.example.android_app.model.LabelUpdateRequest;
import com.example.android_app.model.MailLabelRequest;

import java.util.List;

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

    // --- Label Service Endpoints ---

    @GET("api/labels")
    Call<List<Label>> getLabels(@Header("Authorization") String token);

    @POST("api/labels")
    Call<Label> createLabel(@Header("Authorization") String token, @Body LabelCreateRequest request);

    @PATCH("api/labels/{id}")
    Call<Label> updateLabel(
            @Header("Authorization") String token,
            @Path("id") String id,
            @Body LabelUpdateRequest request
    );

    @DELETE("api/labels/{id}")
    Call<Void> deleteLabel(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @GET("api/labels/{id}/mails")
    Call<List<Mail>> getMailsByLabel(
            @Header("Authorization") String token,
            @Path("id") String id
    );

    @POST("api/labels/{labelId}/mails")
    Call<ResponseBody> addMailToLabel( // Assuming the response might be an empty body or a simple success message
            @Header("Authorization") String token,
            @Path("labelId") String labelId,
            @Body MailLabelRequest request
    );

    // Use @HTTP for DELETE requests that require a request body
    @HTTP(method = "DELETE", path = "api/labels/{labelId}/mails", hasBody = true)
    Call<Void> removeMailFromLabel(
            @Header("Authorization") String token,
            @Path("labelId") String labelId,
            @Body MailLabelRequest request
    );

}
