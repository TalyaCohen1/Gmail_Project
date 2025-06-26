package com.example.android_app.model.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.android_app.data.repository.MailRepository;
import com.example.android_app.model.Email;

public class EmailDetailsViewModel extends AndroidViewModel {

    private final MailRepository mailRepository;
    private final MutableLiveData<Email> emailDetails = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public EmailDetailsViewModel(@NonNull Application application) {
        super(application);
        mailRepository = new MailRepository(application);
    }

    // LiveData שה-Activity יאזין לו כדי לקבל את פרטי המייל
    public LiveData<Email> getEmailDetails() {
        return emailDetails;
    }

    // LiveData שה-Activity יאזין לו כדי לדעת אם הייתה שגיאה
    public LiveData<String> getError() {
        return error;
    }

    // Activate the API call
    public void fetchEmailById(int emailId) {
        mailRepository.getEmailById(emailId, new MailRepository.EmailDetailsCallback() {
            @Override
            public void onSuccess(Email email) {
                emailDetails.postValue(email);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue(errorMessage);
            }
        });
    }
}