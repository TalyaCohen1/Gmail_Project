package com.example.android_app.model.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android_app.data.repository.MailRepository;
import com.example.android_app.model.Email;

import java.util.List;

public class InboxViewModel extends AndroidViewModel {

    private final MailRepository mailRepository;
    private final MutableLiveData<List<Email>> inboxEmails = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();

    public InboxViewModel(@NonNull Application application) {
        super(application);
        mailRepository = new MailRepository(application);
    }

    public LiveData<List<Email>> getInboxEmails() {
        return inboxEmails;
    }

    public LiveData<String> getError() {
        return error;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // method that fetch the data
    public void fetchEmails() {
        Log.d("MyDebug", "ViewModel fetchInbox: Calling repository.getInbox()");
        isLoading.setValue(true);
        mailRepository.getInbox(new MailRepository.InboxCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                inboxEmails.postValue(emails);
                isLoading.postValue(false);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("InboxViewModel", "Failed to fetch inbox: " + errorMessage);
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }
}