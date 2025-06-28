package com.example.android_app.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.android_app.data.network.ApiClient;
import com.example.android_app.data.network.ApiService; // שנה ל-MailApiService אם זה השם הנכון
import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest; // אם קיים, להוספה
import com.example.android_app.utils.SendCallback;
import com.example.android_app.utils.SharedPrefsManager;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MailRepository {

    private final ApiService apiService; // הממשק של Retrofit
    private final Context context;

    public MailRepository(Context context) {
        this.context = context.getApplicationContext();
        apiService = ApiClient.getClient().create(ApiService.class); //create object from retrofit

//        this.context = context.getApplicationContext();
//
//        // ===========================================
//        // === בניית הרשת מרוכזת כאן באופן זמני ===
//        // ===========================================
//        Log.d("MyDebug", "MailRepository Constructor: Building network stack...");
//
//        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
//        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        OkHttpClient client = new OkHttpClient.Builder()
//                .addInterceptor(loggingInterceptor)
//                .build();
//
//        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://192.168.68.50:3000/") // ודאי שזו הכתובת הנכונה
//                .client(client)
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        // ודאי שהשם כאן הוא שם הממשק הנכון (ApiService או MailApiService)
//        this.apiService = retrofit.create(ApiService.class);
//
//        Log.d("MyDebug", "MailRepository Constructor: Network stack built. apiService is " + (this.apiService == null ? "null" : "not null"));
    }

    private String getTokenFromPrefs(Context context) {
        return SharedPrefsManager.get(context, "token");
    }

    // --- מתודות קיימות שנשארות ---

    public void sendEmail(String to, String subject, String body, SendCallback callback) {
        Log.d("MyDebug", "Repository sendEmail: Calling apiService.sendEmail()");
        String token = getTokenFromPrefs(context);
        EmailRequest request = new EmailRequest(to, subject, body); // נניח שזה המודל הנכון

        apiService.sendEmail("Bearer " + token, request).enqueue(new Callback<Void>() {
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
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void getInbox(InboxCallback callback) {
        Log.d("MyDebug", "Repository getInbox: Calling apiService.getInboxEmails()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository getInbox: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getInboxEmails("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(Call<List<Email>> call, Response<List<Email>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Email>> call, Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void getEmailById(String emailId, EmailDetailsCallback callback) {
        Log.d("MyDebug", "Repository getEmailById: Calling apiService.getEmailDetails()");
        String token = getTokenFromPrefs(context);

        apiService.getEmailDetails("Bearer " + token, emailId).enqueue(new Callback<Email>() {
            @Override
            public void onResponse(Call<Email> call, Response<Email> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Email> call, Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    // --- Interfaces שנשארים ---
    public interface InboxCallback {
        void onSuccess(List<Email> emails);
        void onFailure(String error);
    }

    public interface EmailDetailsCallback {
        void onSuccess(Email email);
        void onFailure(String error);
    }
}