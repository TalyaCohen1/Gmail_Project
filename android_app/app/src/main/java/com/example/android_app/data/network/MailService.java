package com.example.android_app.data.network;

import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest;

import java.util.List;

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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:3000/") // local emulator address
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

    public interface MailCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public void getInbox(String token, InboxCallback callback) {
        api.getInboxEmails("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(Call<List<Email>> call, Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch inbox. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Email>> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    // 2. מתודה לקבלת מייל ספציפי לפי ID
    public void getEmailById(int emailId, String token, EmailDetailsCallback callback) {
        api.getEmailDetails("Bearer " + token, emailId).enqueue(new Callback<Email>() {
            @Override
            public void onResponse(Call<Email> call, Response<Email> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch email details. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Email> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    // 3. Interfaces עבור ה-Callbacks החדשים
    public interface InboxCallback {
        void onSuccess(List<Email> emails);
        void onFailure(String error);
    }

    public interface EmailDetailsCallback {
        void onSuccess(Email email);
        void onFailure(String error);
    }
}

