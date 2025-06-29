package com.example.android_app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.android_app.data.network.ApiService;
import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest;
import com.example.android_app.model.Label; // Needed for getMailLabels
import com.example.android_app.model.MailLabelRequest; // Needed for addLabelToMail, removeMailFromLabel
import com.example.android_app.utils.SendCallback;
import com.example.android_app.utils.SharedPrefsManager;

import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody; // Needed for various success/failure responses
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

        // ===========================================
        // === בניית הרשת מרוכזת כאן באופן זמני ===
        // ===========================================
        Log.d("MyDebug", "MailRepository Constructor: Building network stack...");

        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.68.50:3000/") // ודאי שזו הכתובת הנכונה
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        // ודאי שהשם כאן הוא שם הממשק הנכון (ApiService או MailApiService)
        this.apiService = retrofit.create(ApiService.class);

        Log.d("MyDebug", "MailRepository Constructor: Network stack built. apiService is " + (this.apiService == null ? "null" : "not null"));
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

    // --- NEW Mail-Related Methods ---

    public void listMails(ListEmailsCallback callback) {
        Log.d("MyDebug", "Repository listMails: Calling apiService.listMails()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository listMails: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.listMails("Bearer " + token).enqueue(new Callback<List<Email>>() {
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

    public void searchMails(String query, ListEmailsCallback callback) {
        Log.d("MyDebug", "Repository searchMails: Calling apiService.searchMails()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository searchMails: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.searchMails("Bearer " + token, query).enqueue(new Callback<List<Email>>() {
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

    public void getDrafts(ListEmailsCallback callback) {
        Log.d("MyDebug", "Repository getDrafts: Calling apiService.getDrafts()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository getDrafts: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getDrafts("Bearer " + token).enqueue(new Callback<List<Email>>() {
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

    // getInbox is already defined, using apiService.getInboxEmails()

    public void getSentMails(ListEmailsCallback callback) {
        Log.d("MyDebug", "Repository getSentMails: Calling apiService.getSent()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository getSentMails: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getSent("Bearer " + token).enqueue(new Callback<List<Email>>() {
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

    public void getSpamMails(ListEmailsCallback callback) {
        Log.d("MyDebug", "Repository getSpamMails: Calling apiService.getSpamMails()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository getSpamMails: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getSpamMails("Bearer " + token).enqueue(new Callback<List<Email>>() {
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

    public void getDeletedMails(ListEmailsCallback callback) {
        Log.d("MyDebug", "Repository getDeletedMails: Calling apiService.getDeletedMails()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository getDeletedMails: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getDeletedMails("Bearer " + token).enqueue(new Callback<List<Email>>() {
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

    public void markMailAsImportant(String mailId, ActionCallback callback) {
        Log.d("MyDebug", "Repository markMailAsImportant: Calling apiService.markMailAsImportant()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository markMailAsImportant: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markMailAsImportant("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void unmarkMailAsImportant(String mailId, ActionCallback callback) {
        Log.d("MyDebug", "Repository unmarkMailAsImportant: Calling apiService.unmarkMailAsImportant()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository unmarkMailAsImportant: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.unmarkMailAsImportant("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void getImportantMails(ListEmailsCallback callback) {
        Log.d("MyDebug", "Repository getImportantMails: Calling apiService.getImportantMails()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository getImportantMails: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getImportantMails("Bearer " + token).enqueue(new Callback<List<Email>>() {
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

    public void markMailAsStarred(String mailId, ActionCallback callback) {
        Log.d("MyDebug", "Repository markMailAsStarred: Calling apiService.markMailAsStarred()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository markMailAsStarred: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markMailAsStarred("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void unmarkMailAsStarred(String mailId, ActionCallback callback) {
        Log.d("MyDebug", "Repository unmarkMailAsStarred: Calling apiService.unmarkMailAsStarred()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository unmarkMailAsStarred: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.unmarkMailAsStarred("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void getStarredMails(ListEmailsCallback callback) {
        Log.d("MyDebug", "Repository getStarredMails: Calling apiService.getStarredMails()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository getStarredMails: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getStarredMails("Bearer " + token).enqueue(new Callback<List<Email>>() {
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

    // getMail is covered by getEmailById (same endpoint)

    public void updateDraft(String mailId, EmailRequest request, ActionCallback callback) {
        Log.d("MyDebug", "Repository updateDraft: Calling apiService.updateDraft()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository updateDraft: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.updateDraft("Bearer " + token, mailId, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void deleteMail(String mailId, ActionCallback callback) {
        Log.d("MyDebug", "Repository deleteMail: Calling apiService.deleteMail()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository deleteMail: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.deleteMail("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void markMailAsSpam(String mailId, ActionCallback callback) {
        Log.d("MyDebug", "Repository markMailAsSpam: Calling apiService.markMailAsSpam()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository markMailAsSpam: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markMailAsSpam("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void unmarkMailAsSpam(String mailId, ActionCallback callback) {
        Log.d("MyDebug", "Repository unmarkMailAsSpam: Calling apiService.unmarkMailAsSpam()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository unmarkMailAsSpam: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.unmarkMailAsSpam("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void markAsRead(String mailId, ActionCallback callback) {
        Log.d("MyDebug", "Repository markAsRead: Calling apiService.markAsRead()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository markAsRead: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markAsRead("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void markAsUnread(String mailId, ActionCallback callback) {
        Log.d("MyDebug", "Repository markAsUnread: Calling apiService.markAsUnread()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository markAsUnread: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markAsUnread("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void addLabelToMail(String mailId, String labelId, ActionCallback callback) {
        Log.d("MyDebug", "Repository addLabelToMail: Calling apiService.addLabelToMail()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository addLabelToMail: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }
        MailLabelRequest request = new MailLabelRequest(labelId); // Assuming MailLabelRequest takes labelId

        apiService.addLabelToMail("Bearer " + token, mailId, request).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void removeLabelFromMail(String mailId, String labelId, ActionCallback callback) {
        Log.d("MyDebug", "Repository removeLabelFromMail: Calling apiService.removeLabelFromMail()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository removeLabelFromMail: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.removeLabelFromMail("Bearer " + token, mailId, labelId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void getMailLabels(String mailId, ListLabelsCallback callback) {
        Log.d("MyDebug", "Repository getMailLabels: Calling apiService.getMailLabels()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository getMailLabels: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getMailLabels("Bearer " + token, mailId).enqueue(new Callback<List<Label>>() {
            @Override
            public void onResponse(@NonNull Call<List<Label>> call, @NonNull Response<List<Label>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    // NEW method to get mails by label
    public void getMailsByLabel(String labelId, ListEmailsCallback callback) {
        Log.d("MyDebug", "Repository getMailsByLabel: Calling apiService.getMailsByLabel()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository getMailsByLabel: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getMailsByLabel("Bearer " + token, labelId).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Email>> call, @NonNull Throwable t) {
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

    // --- NEW Interfaces for Mail-Related Methods ---

    public interface ListEmailsCallback {
        void onSuccess(List<Email> emails);
        void onFailure(String error);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface ListLabelsCallback {
        void onSuccess(List<Label> labels);
        void onFailure(String error);
    }
}