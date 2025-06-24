package com.example.android_app.data.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import com.example.android_app.BuildConfig;
import com.example.android_app.model.EmailRequest;
import com.example.android_app.utils.SendCallback;


public class MailService {
    private final ApiService api;
    private static final String BASE_URL = BuildConfig.SERVER_URL;
    private Retrofit retrofit;

    public MailService() {
        api = ApiClient.getClient().create(ApiService.class);
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

