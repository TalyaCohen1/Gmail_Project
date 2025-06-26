package com.example.android_app.data.network;

import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface MailApiService {
    @POST("api/mails")
    Call<Void> sendEmail(
            @Header("Authorization") String token,
            @Body EmailRequest email
    );

    @GET("api/mails")
    Call<List<Email>> getInboxEmails(
            @Header("Authorization") String token
    );

    @GET("api/mails/{id}")
    Call<Email> getEmailDetails(
            @Header("Authorization") String token,
            @Path("id") int emailId
    );
}
