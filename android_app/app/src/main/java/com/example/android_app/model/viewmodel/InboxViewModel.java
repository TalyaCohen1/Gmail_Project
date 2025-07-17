package com.example.android_app.model.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android_app.data.repository.MailRepository;
import com.example.android_app.data.repository.MailRepository.LabelsCallback;
import com.example.android_app.model.Email;
import com.example.android_app.model.Label;

import java.util.List;

public class InboxViewModel extends AndroidViewModel {

    private final MailRepository mailRepository;
    private final MutableLiveData<List<Email>> currentEmails = new MutableLiveData<>(); // changed fron inbox to current to generelize the view model
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<List<Label>> labels = new MutableLiveData<>(); //LiveData for labels
    private String currentCategoryOrLabelId = "inbox"; // default to "inbox"

    public InboxViewModel(@NonNull Application application) {
        super(application);
        mailRepository = new MailRepository(application);
        fetchEmailsForCategoryOrLabel("inbox");
        fetchLabels(); // Fetch labels when ViewModel is created
    }

    public LiveData<List<Email>> getCurrentEmails() {
        return currentEmails;
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

    public String getCurrentCategoryOrLabelId() {
        return currentCategoryOrLabelId;
    }

    public void fetchEmailsForCategoryOrLabel(String identifier) {
        currentCategoryOrLabelId = identifier;
        isLoading.postValue(true);


        currentCategoryOrLabelId = identifier; // Save the current identifier
        isLoading.setValue(true);
        error.setValue(null); // clear previous errors

        MailRepository.ListEmailsCallback callback = new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                currentEmails.postValue(emails);
                isLoading.postValue(false);
                error.postValue(null);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue(errorMessage);
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        };

        // choose the appropriate method based on the identifier
        switch (identifier) {
            case "inbox":
                mailRepository.getInbox(callback);
                break;
            case "starred":
                mailRepository.getStarredMails(callback);
                break;
            case "important":
                mailRepository.getImportantMails(callback);
                break;
            case "sent":
                mailRepository.getSentMails(callback);
                break;
            case "drafts":
                mailRepository.getDrafts(callback);
                break;
            case "spam":
                mailRepository.getSpamMails(callback);
                break;
            case "trash":
                mailRepository.getDeletedMails(callback);
                break;
            case "allmail":
                mailRepository.listMails(callback);
                break;
            default:
                mailRepository.getMailsByLabel(identifier, callback);
                break;
        }
    }


    private void updateEmailStatusInList(String emailId, EmailUpdater updater) {
        List<Email> currentEmailsList = currentEmails.getValue();
        if (currentEmailsList != null) {
            List<Email> updatedList = new java.util.ArrayList<>(currentEmailsList);
            boolean found = false;
            for (int i = 0; i < updatedList.size(); i++) {
                Email email = updatedList.get(i);
                if (email.getId().equals(emailId)) {
                    updater.update(email);
                    found = true;
                    break;
                }
            }
            if (found) {
                currentEmails.postValue(updatedList); // update the LiveData with the new list
            }
        }
    }

    private interface EmailUpdater {
        void update(Email email);
    }

    public void deleteEmail(String emailId) {
        mailRepository.deleteMail(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                List<Email> currentEmailsList = currentEmails.getValue();
                if (currentEmailsList != null) {
                    List<Email> updatedList = new java.util.ArrayList<>(currentEmailsList);
                    updatedList.removeIf(email -> email.getId().equals(emailId));
                    currentEmails.postValue(updatedList);
                }
                // Refresh the current emails list after deletion
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to delete email: " + errorMessage);
            }
        });
    }

    public void markEmailAsRead(String emailId) {
        mailRepository.markAsRead(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                updateEmailStatusInList(emailId, email -> email.setIsRead(true));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as read: " + errorMessage);
            }
        });
    }

    public void markEmailAsUnread(String emailId) {
        mailRepository.markAsUnread(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                updateEmailStatusInList(emailId, email -> email.setIsRead(false));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as unread: " + errorMessage);
            }
        });
    }

    public void markEmailAsImportant(String emailId) {
        mailRepository.markMailAsImportant(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                updateEmailStatusInList(emailId, email -> email.setImportant(true));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);

            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as important: " + errorMessage);
            }
        });
    }

    public void unmarkEmailAsImportant(String emailId) {
        mailRepository.unmarkMailAsImportant(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                updateEmailStatusInList(emailId, email -> email.setImportant(false));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to unmark as important: " + errorMessage);
            }
        });
    }

    public void markEmailAsSpam(String emailId) {
        mailRepository.markMailAsSpam(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                List<Email> currentEmailsList = currentEmails.getValue();
                if (currentEmailsList != null) {
                    List<Email> updatedList = new java.util.ArrayList<>(currentEmailsList);
                    updatedList.removeIf(email -> email.getId().equals(emailId));
                    currentEmails.postValue(updatedList); // Update the LiveData immediately
                }
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as spam: " + errorMessage);
            }
        });
    }

    public void markEmailAsStarred(String emailId) {
        mailRepository.markMailAsStarred(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                updateEmailStatusInList(updatedEmailId, email -> email.setStarred(true));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as starred: " + errorMessage);
            }
        });
    }

    public void unmarkEmailAsStarred(String emailId) {
        mailRepository.unmarkMailAsStarred(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                updateEmailStatusInList(emailId, email -> email.setStarred(false));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to unmark as starred: " + errorMessage);
            }
        });
    }

    public void fetchLabels() {
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

    