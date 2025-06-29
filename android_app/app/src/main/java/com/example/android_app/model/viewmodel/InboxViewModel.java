package com.example.android_app.model.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android_app.data.repository.MailRepository;
import com.example.android_app.model.Email;
import com.example.android_app.model.Label; // Import for Label
import com.example.android_app.data.repository.MailRepository.MailActionCallback; // Import for MailActionCallback
import com.example.android_app.data.repository.MailRepository.LabelsCallback; // Import for LabelsCallback

import java.util.List;

public class InboxViewModel extends AndroidViewModel {

    private final MailRepository mailRepository;
    private final MutableLiveData<List<Email>> inboxEmails = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<List<Label>> labels = new MutableLiveData<>(); // New: LiveData for labels

    public InboxViewModel(@NonNull Application application) {
        super(application);
        mailRepository = new MailRepository(application);
        fetchLabels(); // Fetch labels when ViewModel is created
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

    public LiveData<List<Label>> getLabels() { // New getter for labels
        return labels;
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
                error.postValue(null); // Clear any previous errors
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("InboxViewModel", "Failed to fetch inbox: " + errorMessage);
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    public void deleteEmail(String emailId) {
        // Optionally show a loading state for this specific action
        mailRepository.deleteMail(emailId, new MailActionCallback() {
            @Override
            public void onSuccess() {
                Log.d("InboxViewModel", "Email deleted: " + emailId);
                fetchEmails(); // Refresh the inbox after deletion
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to delete email: " + errorMessage);
            }
        });
    }

    public void markEmailAsRead(String emailId) {
        mailRepository.markAsRead(emailId, new MailActionCallback() {
            @Override
            public void onSuccess() {
                Log.d("InboxViewModel", "Email marked as read: " + emailId);
                fetchEmails(); // Refresh after action
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as read: " + errorMessage);
            }
        });
    }

    public void markEmailAsUnread(String emailId) {
        mailRepository.markAsUnread(emailId, new MailActionCallback() {
            @Override
            public void onSuccess() {
                Log.d("InboxViewModel", "Email marked as unread: " + emailId);
                fetchEmails(); // Refresh after action
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as unread: " + errorMessage);
            }
        });
    }

    public void addLabelToEmail(String emailId, String labelId) {
        mailRepository.addLabelToMail(emailId, labelId, new MailActionCallback() {
            @Override
            public void onSuccess() {
                Log.d("InboxViewModel", "Label " + labelId + " added to email " + emailId);
                fetchEmails(); // Refresh after action
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to add label: " + errorMessage);
            }
        });
    }

    public void markEmailAsImportant(String emailId) {
        mailRepository.markMailAsImportant(emailId, new MailActionCallback() {
            @Override
            public void onSuccess() {
                Log.d("InboxViewModel", "Email marked as important: " + emailId);
                fetchEmails();
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as important: " + errorMessage);
            }
        });
    }

    public void markEmailAsSpam(String emailId) {
        mailRepository.markMailAsSpam(emailId, new MailActionCallback() {
            @Override
            public void onSuccess() {
                Log.d("InboxViewModel", "Email marked as spam: " + emailId);
                fetchEmails();
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as spam: " + errorMessage);
            }
        });
    }


    public void fetchLabels() { // New method to fetch labels
        mailRepository.getLabels(new LabelsCallback() {
            @Override
            public void onSuccess(List<Label> fetchedLabels) {
                labels.postValue(fetchedLabels);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to fetch labels: " + errorMessage);
            }
        });
    }
}