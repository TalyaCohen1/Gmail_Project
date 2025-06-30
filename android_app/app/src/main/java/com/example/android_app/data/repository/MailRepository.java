package com.example.android_app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.android_app.data.network.ApiClient;
import com.example.android_app.data.network.ApiService;
import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest;
import com.example.android_app.model.Label; // Needed for getMailLabels
import com.example.android_app.model.MailLabelRequest; // Needed for addLabelToMail, removeMailFromLabel
import com.example.android_app.utils.MailMapper;
import com.example.android_app.utils.SendCallback;
import com.example.android_app.utils.SharedPrefsManager;
import com.example.android_app.data.network.MailService;

//Room Database
import com.example.android_app.data.local.AppDatabase;
import com.example.android_app.data.local.MailDAO;
import com.example.android_app.data.local.MailEntity;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody; // Needed for various success/failure responses
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MailRepository {

    private final ApiService apiService; // הממשק של Retrofit
    private final Context context;
    private final MailDAO mailDao;
    private final MailService mailService = new MailService();

    private final ExecutorService executor; //so room run in another thread

    public MailRepository(Context context) {
        this.context = context.getApplicationContext();
        apiService = ApiClient.getClient().create(ApiService.class); //create object from retrofit
        AppDatabase db = AppDatabase.getInstance(context);
        mailDao = db.mailDao();
        executor = Executors.newSingleThreadExecutor();

    }

    private String getTokenFromPrefs(Context context) {
        return SharedPrefsManager.get(context, "token");
    }

    // --- מתודות קיימות שנשארות ---

    public void sendEmail(String to, String subject, String body, SendCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }
        EmailRequest request = new EmailRequest(to, subject, body);

        apiService.sendEmail("Bearer " + token, request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void getInbox(InboxCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getInboxEmails("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(Call<List<Email>> call, Response<List<Email>> response) {
                if (response.isSuccessful()) {
                    List<Email> emails = response.body();
                    saveEmailsToLocalDb(emails);
                    callback.onSuccess(emails);
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

    public void getEmailById(String emailId, EmailDetailsCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getEmailDetails("Bearer " + token, emailId).enqueue(new Callback<Email>() {
            @Override
            public void onResponse(@NonNull Call<Email> call, @NonNull Response<Email> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());

                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }
            @Override
            public void onFailure(@NonNull Call<Email> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

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
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
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
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
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

    public void getDrafts(ListEmailsCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getDrafts("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(Call<List<Email>> call, Response<List<Email>> response) {
                if (response.isSuccessful()) {
                    List<Email> emails = response.body();
                    saveEmailsToLocalDb(emails);
                    callback.onSuccess(emails);
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
                    List<Email> emails = response.body();
                    saveEmailsToLocalDb(emails);
                    callback.onSuccess(emails);                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Email>> call, @NonNull Throwable t) {
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
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Email> emails = response.body();
                    saveEmailsToLocalDb(emails);
                    callback.onSuccess(emails);                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Email>> call, @NonNull Throwable t) {
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
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Email> emails = response.body();
                    saveEmailsToLocalDb(emails);
                    callback.onSuccess(emails);                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Email>> call, @NonNull Throwable t) {
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
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
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
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
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

    public void updateDraft(String mailId, EmailRequest request, MailActionCallback callback) {
        Log.d("MyDebug", "Repository updateDraft: Calling apiService.updateDraft()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository updateDraft: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.updateDraft("Bearer " + token, mailId, request).enqueue(new Callback<ResponseBody>() {
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


    public void deleteMail(String emailId, MailActionCallback callback) { // Updated to use MailActionCallback
        Log.d("MyDebug", "Repository deleteMail: Calling apiService.deleteMail()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.deleteMail("Bearer " + token, emailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Failed to delete mail. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void markAsRead(String emailId, MailActionCallback callback) { // Updated to use MailActionCallback
        Log.d("MyDebug", "Repository markAsRead: Calling apiService.markAsRead()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markAsRead("Bearer " + token, emailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Failed to mark as read. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void markAsUnread(String emailId, MailActionCallback callback) { // Updated to use MailActionCallback
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markAsUnread("Bearer " + token, emailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                } else {
                    callback.onFailure("Failed to mark as unread. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void markMailAsImportant(String emailId, MailActionCallback callback) { // Updated to use MailActionCallback
        Log.d("MyDebug", "Repository markMailAsImportant: Calling apiService.markMailAsImportant()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository markMailAsImportant: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markMailAsImportant("Bearer " + token, emailId).enqueue(new Callback<ResponseBody>() {
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

    public void unmarkMailAsImportant(String mailId, MailActionCallback callback) { // Updated to use MailActionCallback
        Log.d("MyDebug", "Repository unmarkMailAsImportant: Calling apiService.unmarkMailAsImportant()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository unmarkMailAsImportant: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.unmarkMailAsImportant("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
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

    public void markMailAsStarred(String mailId, MailActionCallback callback) { // Updated to use MailActionCallback
        Log.d("MyDebug", "Repository markMailAsStarred: Calling apiService.markMailAsStarred()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository markMailAsStarred: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markMailAsStarred("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
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

    public void unmarkMailAsStarred(String mailId, MailActionCallback callback) { // Updated to use MailActionCallback
        Log.d("MyDebug", "Repository unmarkMailAsStarred: Calling apiService.unmarkMailAsStarred()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository unmarkMailAsStarred: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.unmarkMailAsStarred("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
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

    public void markMailAsSpam(String mailId, MailActionCallback callback) { // Updated to use MailActionCallback
        Log.d("MyDebug", "Repository markMailAsSpam: Calling apiService.markMailAsSpam()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository markMailAsSpam: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markMailAsSpam("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
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

    public void unmarkMailAsSpam(String mailId, MailActionCallback callback) { // Updated to use MailActionCallback
        Log.d("MyDebug", "Repository unmarkMailAsSpam: Calling apiService.unmarkMailAsSpam()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            Log.e("MyDebug", "Repository unmarkMailAsSpam: TOKEN IS MISSING!");
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.unmarkMailAsSpam("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
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

    public void addLabelToMail(String mailId, String labelId, MailActionCallback callback) { // Updated to use MailActionCallback
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

    public void removeLabelFromMail(String mailId, String labelId, MailActionCallback callback) { // Updated to use MailActionCallback
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

    public void getMailLabels(String mailId, LabelsCallback callback) { // Updated to use LabelsCallback
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
                if (response.isSuccessful() && response.body() != null) {
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
                    List<Email> emails = response.body();
                    saveEmailsToLocalDb(emails);
                    callback.onSuccess(emails);                } else {
                    callback.onFailure("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Email>> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }


        private void saveEmailsToLocalDb(List<Email> emails) {
            executor.execute(() -> {
                List<MailEntity> entities = new ArrayList<>();
                for (Email email : emails) {
                    entities.add(MailMapper.toEntity(email));
                }
                mailDao.insertAll(entities);
            });
        }

        public void getMailsFromLocal(LocalCallback<List<MailEntity>> callback) {
            executor.execute(() -> {
                List<MailEntity> mails = mailDao.getAllMailsNow();
                callback.onResult(mails);
            });
        }
    public void syncInboxFromServer() {
        getInbox(new InboxCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                executor.execute(() -> {
                    List<MailEntity> entities = MailMapper.toEntities(emails);
                    mailDao.clearAll();
                    mailDao.insertAll(entities);
                });
            }

            @Override
            public void onFailure(String error) {
                Log.e("Repo", "Failed to sync: " + error);
            }
        });
    }

    public LiveData<List<MailEntity>> getLocalInbox() {
        return mailDao.getAllMails();
    }

    public void fetchInboxAndSaveToLocal(ActionCallback callback) {
        String token = getTokenFromPrefs(context);

        mailService.getInbox(token, new MailService.InboxCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                // המרת Email ל־MailEntity
                List<MailEntity> entities = MailMapper.toEntities(emails);

                // שמירה במסד המקומי (Room)
                executor.execute(() -> {
                    mailDao.insertAll(entities); // פעולה שכותבת במסד המקומי
                    callback.onSuccess();
                });
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }




    // NEW method to fetch all labels (for the "Add to label" menu in InboxActivity)
    public void getLabels(LabelsCallback callback) {
        Log.d("MyDebug", "Repository getLabels: Calling apiService.getLabels()");
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getLabels("Bearer " + token).enqueue(new Callback<List<Label>>() {
            @Override
            public void onResponse(@NonNull Call<List<Label>> call, @NonNull Response<List<Label>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onFailure("Failed to fetch labels. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Label>> call, @NonNull Throwable t) {
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

    public interface MailActionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface LocalCallback<T> {
        void onResult(T result);
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

    public interface LabelsCallback { // Changed from ListLabelsCallback to LabelsCallback for clarity and consistency
        void onSuccess(List<Label> labels);
        void onFailure(String error);
    }
    public interface ListLabelsCallback {
        void onSuccess(List<Label> labels);
        void onFailure(String error);
    }
}