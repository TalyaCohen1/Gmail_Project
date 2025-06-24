package com.example.android_app.data.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;


import com.example.android_app.BuildConfig;
import com.example.android_app.model.EmailRequest;
import com.example.android_app.data.network.MailApiService;
import com.example.android_app.utils.SendCallback;


public class MailService {
    private final MailApiService api;
    private static final String BASE_URL = BuildConfig.SERVER_URL;
    private Retrofit retrofit;

    public MailService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

    api = retrofit.create(MailApiService.class);
    }

public void sendEmail(String to, String subject, String body, String token, SendCallback callback) {
    EmailRequest request = new EmailRequest(to, subject, body);
    api.sendEmail("Bearer " + token, request).enqueue(new Callback<Void>() {
        @Override
        public void onResponse(Call<Void> call, Response<Void> response) {
            if (response.isSuccessful()) {
                callback.onSuccess();
            } else {
                callback.onFailure("Server error: " + response.code());
            }
        }

        @Override
        public void onFailure(Call<Void> call, Throwable t) {
            callback.onFailure(t.getMessage());
        }
    });
}
}

