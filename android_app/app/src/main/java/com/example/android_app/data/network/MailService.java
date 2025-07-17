package com.example.android_app.data.network;

import com.example.android_app.BuildConfig;
import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest;
import android.util.Log;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MailService {
    private final ApiService api;
    private static final String BASE_URL = BuildConfig.SERVER_URL;
    private Retrofit retrofit;

    public MailService() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ApiService.class);
    }

    // Method to create a draft (using EmailRequest directly as discussed previously)
    public void createDraft(String token, EmailRequest request, DraftMailCallback callback) {
        request.setSend(false); // Set 'send' to false for drafts
        api.createDraft("Bearer " + token, request).enqueue(new Callback<Email>() {
            @Override
            public void onResponse(Call<Email> call, Response<Email> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to create draft. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Email> call, Throwable t) {
                Log.e("MailService", "Network error creating draft: " + t.getMessage(), t); // הוסף לוג
                callback.onFailure("Network error creating draft: " + t.getMessage());
            }
        });
    }

    // Method to update a draft
    public void updateDraft(String mailId, String to, String subject, String body, String token, DraftMailCallback callback) {
        EmailRequest request = new EmailRequest(to, subject, body);
        request.setSend(false);
        api.updateDraft("Bearer " + token, mailId, request).enqueue(new Callback<Email>() {
            @Override
            public void onResponse(Call<Email> call, Response<Email> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to update draft. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Email> call, Throwable t) {
               callback.onFailure("Network error updating draft: " + t.getMessage());
            }
        });
    }

    // Method to send a draft
    // This method sends a draft email by its ID and token, using the EmailRequest object
    public void sendDraft(String mailId, String token, EmailRequest request, SendDraftCallback callback) {
        api.sendDraft(mailId, "Bearer " + token, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onFailure("Failed to send draft: " + errorBody);
                    } catch (IOException e) {
                        callback.onFailure("Failed to send draft: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }


    // Method to send a new email
    // This method sends a new email using the EmailRequest object and token
    public void sendEmail(String token, EmailRequest request, SendEmailCallback callback) {
        api.sendEmail("Bearer " + token, request).enqueue(new Callback<ResponseBody>() { 
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        callback.onFailure("Failed to send email: " + errorBody);
                    } catch (IOException e) {
                        callback.onFailure("Failed to send email: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    // Method to get inbox emails
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

    // Method to get a specific email by ID
    public void getEmailById(String emailId, String token, EmailDetailsCallback callback) {
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


// Callback interfaces for various operations
    // These interfaces are used to handle the results of network operations in a clean way.
    public interface DraftMailCallback {
        void onSuccess(Email email); // Returns the saved/updated draft
        void onFailure(String error);
    }

    public interface InboxCallback {
        void onSuccess(List<Email> emails);
        void onFailure(String error);
    }

    public interface EmailDetailsCallback {
        void onSuccess(Email email);
        void onFailure(String error);
    }

    public interface SendEmailCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface SendDraftCallback {
        void onSuccess();
        void onFailure(String error);
    }
}