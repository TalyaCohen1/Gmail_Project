package com.example.android_app.data.repository;

import android.content.Context;

import com.example.android_app.data.network.MailService;
import com.example.android_app.model.Email;
import com.example.android_app.utils.SendCallback;
import com.example.android_app.utils.SharedPrefsManager;

import java.util.List;

public class MailRepository {
    private final MailService mailService;
    private final Context context;

    public MailRepository(Context context) {
        this.mailService = new MailService();
        this.context = context.getApplicationContext();
    }

    public void sendEmail(String to, String subject, String body, SendCallback callback) {
        String token = getTokenFromPrefs(context); // מתוך SharedPreferences
        mailService.sendEmail(to, subject, body, token, new MailService.MailCallback() {
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
    private String getTokenFromPrefs(Context context) {
        return SharedPrefsManager.get(context, "token");
    }

    public void getInbox(InboxCallback callback) {
        String token = getTokenFromPrefs(context);
        mailService.getInbox(token, new MailService.InboxCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                callback.onSuccess(emails);
            }

            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    // 2. מתודה לקבלת מייל ספציפי שה-ViewModel יקרא לה
    public void getEmailById(int emailId, EmailDetailsCallback callback) {
        String token = getTokenFromPrefs(context);
        mailService.getEmailById(emailId, token, new MailService.EmailDetailsCallback() {
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

    // 3. Interfaces עבור ה-Callbacks שה-ViewModel ישתמש בהם
    public interface InboxCallback {
        void onSuccess(List<Email> emails);
        void onFailure(String error);
    }

    public interface EmailDetailsCallback {
        void onSuccess(Email email);
        void onFailure(String error);
    }
}
