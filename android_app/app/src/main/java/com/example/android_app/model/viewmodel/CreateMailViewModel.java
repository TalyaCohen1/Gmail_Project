package com.example.android_app.model.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.android_app.data.repository.MailRepository;
import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;
import com.example.android_app.utils.SendCallback;

public class CreateMailViewModel extends AndroidViewModel {

    private final MailRepository repository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emailSent = new MutableLiveData<>();


    public CreateMailViewModel(@NonNull Application application) {
        super(application);
        repository = new MailRepository(application);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void sendEmail(String to, String subject, String body) {
        if (TextUtils.isEmpty(to)) {
            errorMessage.setValue("Recipient is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(to).matches()) {
            errorMessage.setValue("Invalid email address");
            return;
        }

        repository.sendEmail(to, subject, body, new SendCallback() {
            @Override
            public void onSuccess() {
                emailSent.setValue(true);
                errorMessage.setValue(null); // clear
            }

            @Override
            public void onFailure(String error) {
                emailSent.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }
    public LiveData<Boolean> getEmailSent() {
        return emailSent;
    }
}

