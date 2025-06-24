package com.example.android_app.data.repository;
import android.content.Context;
import com.example.android_app.data.network.MailService;
import com.example.android_app.model.viewmodel.CreateMailViewModel;
import com.example.android_app.utils.SendCallback;
import com.example.android_app.utils.SharedPrefsManager;
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
}
