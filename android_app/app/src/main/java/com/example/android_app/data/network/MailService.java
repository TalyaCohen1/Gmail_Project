package com.example.android_app.data.network;

//import com.example.android_app.BuildConfig;
import com.example.android_app.BuildConfig;
import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest;
import com.example.android_app.utils.SendCallback; // וודא שזה מיובא
import android.util.Log; // הוסף ייבוא עבור Log אם חסר
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

    // New: Method to create a draft (using EmailRequest directly as discussed previously)
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
                    Log.e("MailService", "Error creating draft: " + errorMsg); // הוסף לוג
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

    // New: Method to update a draft
    public void updateDraft(String mailId, String to, String subject, String body, String token, DraftMailCallback callback) {
        EmailRequest request = new EmailRequest(to, subject, body);
        // If EmailRequest needs 'isDraft': request.setDraft(true); // אם יש לך שדה כזה ב-EmailRequest
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
                    Log.e("MailService", "Error updating draft: " + errorMsg); // הוסף לוג
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Email> call, Throwable t) {
                Log.e("MailService", "Network error updating draft: " + t.getMessage(), t); // הוסף לוג
                callback.onFailure("Network error updating draft: " + t.getMessage());
            }
        });
    }

    // New: Method to send a draft
    public void sendDraft(String mailId, String token, SendDraftCallback callback) {
        api.sendDraft("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() { // Or <Email>
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String errorMsg = "Failed to send draft. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ": " + response.errorBody().string();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Log.e("MailService", "Error sending draft: " + errorMsg); // הוסף לוג
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("MailService", "Network error sending draft: " + t.getMessage(), t); // הוסף לוג
                callback.onFailure("Network error sending draft: " + t.getMessage());
            }
        });
    }

    // Existing sendEmail method (for new emails, not drafts)
    public void sendEmail(String to, String subject, String body, String token, SendCallback callback) {
        EmailRequest request = new EmailRequest(to, subject, body);
        api.sendEmail("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    String errorMsg = "Failed to send email. Code: " + response.code();
                    // ניתן להוסיף קריאה ל-response.errorBody().string() כאן גם
                    Log.e("MailService", "Error sending email: " + errorMsg); // הוסף לוג
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("MailService", "Network error sending email: " + t.getMessage(), t); // הוסף לוג
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
                    Log.e("MailService", "Failed to fetch inbox. Code: " + response.code());
                    callback.onFailure("Failed to fetch inbox. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Email>> call, Throwable t) {
                Log.e("MailService", "Network error fetching inbox: " + t.getMessage(), t);
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
                    Log.e("MailService", "Failed to fetch email details. Code: " + response.code());
                    callback.onFailure("Failed to fetch email details. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Email> call, Throwable t) {
                Log.e("MailService", "Network error fetching email details: " + t.getMessage(), t);
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

    // --- Callback Interfaces (מועברות לסוף הקובץ ומוגדרות פעם אחת בלבד) ---

    public interface DraftMailCallback {
        void onSuccess(Email email); // Returns the saved/updated draft
        void onFailure(String error);
    }

    public interface SendDraftCallback {
        void onSuccess(); // Indicates the draft was successfully sent
        void onFailure(String error);
    }

    public interface MailCallback { // ייתכן שזהו ממשק כללי יותר, אם לא בשימוש, ניתן להסירו
        void onSuccess();
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
}