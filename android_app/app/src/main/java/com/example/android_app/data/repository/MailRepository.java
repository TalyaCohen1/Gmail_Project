package com.example.android_app.data.repository;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import com.example.android_app.data.local.AppDatabase;
import com.example.android_app.data.local.MailDAO;
import com.example.android_app.data.local.MailEntity;
import com.example.android_app.data.local.UserDao;
import com.example.android_app.data.network.ApiClient;
import com.example.android_app.data.network.ApiService;
import com.example.android_app.data.network.MailService;
import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest;
import com.example.android_app.model.Label;
import com.example.android_app.model.MailLabelRequest;
import com.example.android_app.utils.MailMapper;
import com.example.android_app.utils.SendCallback;
import com.example.android_app.utils.SharedPrefsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// This class serves as a repository for managing email-related operations.
// It interacts with both the local database (Room) and remote API (Retrofit) to
public class MailRepository {

    private final ApiService apiService;
    private final Context context;
    private final MailDAO mailDao;
    private final UserDao userDao;
    private final MailService mailService = new MailService();
    private final ExecutorService executor; //so room run in another thread
    private final Handler mainThreadHandler; 

    public MailRepository(Context context) {
        this.context = context.getApplicationContext();
        apiService = ApiClient.getClient().create(ApiService.class); //create object from retrofit
        AppDatabase db = AppDatabase.getInstance(context);
        mailDao = db.mailDao();
        this.userDao = db.userDao();
        executor = Executors.newSingleThreadExecutor();
        mainThreadHandler = new Handler(context.getMainLooper());
    }

    private String getTokenFromPrefs(Context context) {
        return SharedPrefsManager.get(context, "token");
    }

    public void getCurrentUserEmail(LocalCallback<String> callback) {
        executor.execute(() -> {
            String email = SharedPrefsManager.get(context, "emailAddress");
            callback.onResult(email);
        });
    }

    private void filterVisibleAsync(List<Email> emails, LocalCallback<List<Email>> callback) {
        getCurrentUserEmail(currentUserEmail -> {
            List<Email> filtered = new ArrayList<>();
            for (Email email : emails) {
                String from = email.getFrom();
                String to = email.getTo();
                boolean isSender = from != null && currentUserEmail != null && from.equalsIgnoreCase(currentUserEmail);
                boolean isReceiver = to != null && currentUserEmail != null && to.equalsIgnoreCase(currentUserEmail);

                boolean visibleToSender = isSender && !email.isDeletedForSender();
                boolean visibleToReceiver = isReceiver && !email.isDeletedForReceiver();

                if (visibleToSender || visibleToReceiver) {
                    filtered.add(email);
                }
            }
            callback.onResult(filtered);
        });
    }

    private void filterDeletedAsync(List<Email> emails, LocalCallback<List<Email>> callback) {
        getCurrentUserEmail(currentUserEmail -> {
            List<Email> filtered = new ArrayList<>();
            for (Email email : emails) {
                String from = email.getFrom();
                String to = email.getTo();
                boolean isSender = from != null && currentUserEmail != null && from.equalsIgnoreCase(currentUserEmail);
                boolean isReceiver = to != null && currentUserEmail != null && to.equalsIgnoreCase(currentUserEmail);

                boolean deletedBySender = isSender && email.isDeletedForSender();
                boolean deletedByReceiver = isReceiver && email.isDeletedForReceiver();

                if (deletedBySender || deletedByReceiver) {
                    filtered.add(email);
                }
            }
            callback.onResult(filtered);
        });
    }
    // This method creates a new draft email.
    public void createDraft(EmailRequest request, MailService.DraftMailCallback callback) {
        String token = SharedPrefsManager.get(context, "token");
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token missing.");
            return;
        }
        mailService.createDraft(token, request, new MailService.DraftMailCallback() {
            @Override
            public void onSuccess(Email email) {
                callback.onSuccess(email);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

// This method updates an existing draft email - after it has been created.
    public void updateDraft(String mailId, String to, String subject, String body, String token, MailService.DraftMailCallback callback) {
        mailService.updateDraft(mailId, to, subject, body, token, new MailService.DraftMailCallback() {
            @Override
            public void onSuccess(Email email) {
                // Optionally update in local DB here
                callback.onSuccess(email);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

// This method sends an email, either as a new email or as a draft.
    public void sendEmail(String mailId, String to, String subject, String body, String token, SendCallback callback) {
        //update to send = true so we can send it, either if it is a new draft or an existing one
        EmailRequest emailRequest = new EmailRequest(to, subject, body, true);

        if (mailId != null && !mailId.isEmpty()) {
            //if mailId exist- its draft, just PATCH
            mailService.sendDraft(mailId, token, emailRequest, new MailService.SendDraftCallback() {
                @Override
                public void onSuccess() {
                    callback.onSuccess();
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                }
            });
        } else {
            // if there is no MailId- its new mail :)
            mailService.sendEmail(token, emailRequest, new MailService.SendEmailCallback() {
                @Override
                public void onSuccess() {
                    callback.onSuccess();
                }

                @Override
                public void onFailure(String error) {
                    callback.onFailure(error);
                }
            });
        }
    }

    public void deleteDraft(String mailId, ActionCallback callback) {
        executor.execute(() -> {
            try {
                MailEntity mailToDelete = mailDao.getMailByIdNow(mailId);
                if (mailToDelete != null) {
                    mailDao.deleteMail(mailToDelete);
                    mainThreadHandler.post(callback::onSuccess);
                } else {
                    mainThreadHandler.post(() -> callback.onFailure("Draft not found locally: " + mailId));
                }
            } catch (Exception e) {
                mainThreadHandler.post(() -> callback.onFailure("Failed to delete draft from local DB: " + e.getMessage()));
            }
        });
    }


    public void getInbox(ListEmailsCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getInboxEmails("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(Call<List<Email>> call, Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterVisibleAsync(response.body(), filtered -> {
                        saveEmailsToLocalDb(filtered);
                        callback.onSuccess(filtered);
                    });
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
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.listMails("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterVisibleAsync(response.body(), filtered -> {
                        saveEmailsToLocalDb(filtered);
                        callback.onSuccess(filtered);
                    });
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
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
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
        String token = SharedPrefsManager.get(context, "token");
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getDrafts("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(Call<List<Email>> call, Response<List<Email>> response) {
                if (response.isSuccessful()) {
                    filterVisibleAsync(response.body(), filtered -> {
                        saveEmailsToLocalDb(filtered);
                        callback.onSuccess(filtered);
                    });
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
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getSent("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(Call<List<Email>> call, Response<List<Email>> response) {
                if (response.isSuccessful()) {
                    filterVisibleAsync(response.body(), filtered -> {
                        saveEmailsToLocalDb(filtered);
                        callback.onSuccess(filtered);
                    });
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

    public void getSpamMails(ListEmailsCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getSpamMails("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterVisibleAsync(response.body(), filtered -> {
                        saveEmailsToLocalDb(filtered);
                        callback.onSuccess(filtered);
                    });
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

    public void getDeletedMails(ListEmailsCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getDeletedMails("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterDeletedAsync(response.body(), filtered -> {
                        saveEmailsToLocalDb(filtered);
                        callback.onSuccess(filtered);
                    });
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

    public void getImportantMails(ListEmailsCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getImportantMails("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterVisibleAsync(response.body(), filtered -> {
                        saveEmailsToLocalDb(filtered);
                        callback.onSuccess(filtered);
                    });
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
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getStarredMails("Bearer " + token).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterVisibleAsync(response.body(), filtered -> {
                        saveEmailsToLocalDb(filtered);
                        callback.onSuccess(filtered);
                    });
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
    public void deleteMail(String mailId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.deleteMail("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(mailId);
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

    public void markAsRead(String mailId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markAsRead("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // ** Update Room Database **
                    executor.execute(() -> {
                        MailEntity existingMail = mailDao.getMailByIdNow(mailId);
                        if (existingMail != null) {
                            existingMail.isRead = true;
                            mailDao.insertMail(existingMail);
                        }
                    });
                    callback.onSuccess(mailId);
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

    public void markAsUnread(String mailId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markAsUnread("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        MailEntity existingMail = mailDao.getMailByIdNow(mailId);
                        if (existingMail != null) {
                            existingMail.isRead = false;
                            mailDao.insertMail(existingMail);
                        }
                    });
                    callback.onSuccess(mailId);
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

    public void markMailAsImportant(String mailId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markMailAsImportant("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        MailEntity existingMail = mailDao.getMailByIdNow(mailId);
                        if (existingMail != null) {
                            existingMail.isImportant = true;
                            mailDao.insertMail(existingMail);
                        }
                        new Handler(context.getMainLooper()).post(() -> callback.onSuccess(mailId));
                    });
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

    public void unmarkMailAsImportant(String mailId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.unmarkMailAsImportant("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        MailEntity existingMail = mailDao.getMailByIdNow(mailId);
                        if (existingMail != null) {
                            existingMail.isImportant = false;
                            mailDao.insertMail(existingMail);
                        }
                        new Handler(context.getMainLooper()).post(() -> callback.onSuccess(mailId));
                    });
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

    public void markMailAsStarred(String mailId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markMailAsStarred("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        MailEntity existingMail = mailDao.getMailByIdNow(mailId);
                        if (existingMail != null) {
                            existingMail.isStarred = true;
                            mailDao.insertMail(existingMail);
                        }
                        new Handler(context.getMainLooper()).post(() -> callback.onSuccess(mailId));
                    });
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

    public void unmarkMailAsStarred(String mailId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.unmarkMailAsStarred("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        MailEntity existingMail = mailDao.getMailByIdNow(mailId);
                        if (existingMail != null) {
                            existingMail.isStarred = false;
                            mailDao.insertMail(existingMail);
                        }
                        new Handler(context.getMainLooper()).post(() -> callback.onSuccess(mailId));
                    });
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

    public void markMailAsSpam(String mailId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.markMailAsSpam("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        MailEntity existingMail = mailDao.getMailByIdNow(mailId);
                        if (existingMail != null) {
                            existingMail.isSpam = true;
                            mailDao.insertMail(existingMail);
                        }
                    });
                    callback.onSuccess(mailId);
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

    public void unmarkMailAsSpam(String mailId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.unmarkMailAsSpam("Bearer " + token, mailId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        MailEntity existingMail = mailDao.getMailByIdNow(mailId);
                        if (existingMail != null) {
                            existingMail.isSpam = false;
                            mailDao.insertMail(existingMail);
                        } else {
                            Log.w("MailRepository", "Mail not found in local DB, cannot update: " + mailId);
                        }
                    });
                    callback.onSuccess(mailId);
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

    public void addMailToLabel(String emailId, String labelId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        MailLabelRequest requestBody = new MailLabelRequest(emailId);

        apiService.addMailToLabel("Bearer " + token, labelId, requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(emailId);
                } else {
                    String errorMsg = "Failed to add label. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ", Body: " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("MailRepository", "Error reading error body", e);
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void removeMailFromLabel(String emailId, String labelId, MailActionCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        MailLabelRequest requestBody = new MailLabelRequest(emailId);

        apiService.removeMailFromLabel("Bearer " + token, labelId, requestBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(emailId);
                } else {
                    String errorMsg = "Failed to remove label. Code: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += ", Body: " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e("MailRepository", "Error reading error body", e);
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onFailure("Network failure: " + t.getMessage());
            }
        });
    }

    public void getMailsByLabel(String labelId, ListEmailsCallback callback) {
        String token = getTokenFromPrefs(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }

        apiService.getMailsByLabel("Bearer " + token, labelId).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(@NonNull Call<List<Email>> call, @NonNull Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterVisibleAsync(response.body(), filtered -> {
                        saveEmailsToLocalDb(filtered);
                        callback.onSuccess(filtered);
                    });
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


        private void saveEmailsToLocalDb(List<Email> emails) {
            executor.execute(() -> {
                List<MailEntity> entities = new ArrayList<>();
                for (Email email : emails) {
                    if (email.getId() == null) {
                        continue;
                    }
                    entities.add(MailMapper.toEntity(email));
                }
                mailDao.insertAll(entities);
            });
        }

    public void fetchInboxAndSaveToLocal(ActionCallback callback) {
        String token = getTokenFromPrefs(context);

        mailService.getInbox(token, new MailService.InboxCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                List<MailEntity> entities = MailMapper.toEntities(emails);
                executor.execute(() -> {
                    mailDao.insertAll(entities);
                    callback.onSuccess();
                });
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    //Method to fetch all labels (for the "Add to label" menu in InboxActivity)
    public void getLabels(LabelsCallback callback) {
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

    public interface EmailDetailsCallback {
        void onSuccess(Email email);
        void onFailure(String error);
    }

    public interface MailActionCallback {
        void onSuccess(String emailId);
        void onFailure(String error);
    }

    public interface LocalCallback<T> {
        void onResult(T result);
    }

    public interface ListEmailsCallback {
        void onSuccess(List<Email> emails);
        void onFailure(String error);
    }

    public interface ActionCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface LabelsCallback {
        void onSuccess(List<Label> labels);
        void onFailure(String error);
    }
}