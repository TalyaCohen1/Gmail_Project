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
    private final MutableLiveData<List<Label>> labels = new MutableLiveData<>(); // New: LiveData for labels

    private String currentCategoryOrLabelId = "inbox"; // default to "inbox"

    public InboxViewModel(@NonNull Application application) {
        super(application);
        mailRepository = new MailRepository(application);
        fetchEmailsForCategoryOrLabel("inbox");
        fetchLabels(); // Fetch labels when ViewModel is created
    }

    //    public LiveData<List<MailEntity>> getInboxEmails() {
//        return mailRepository.getLocalInbox();
//    }
    public LiveData<List<Email>> getCurrentEmails() {
        return currentEmails;
    }

    public LiveData<String> getError() {
        return error;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    //    public void refreshInboxFromServer() {
//        mailRepository.syncInboxFromServer();
//    }
    public void refreshInboxFromServer() {
        isLoading.setValue(true);
        mailRepository.syncInboxFromServer();
    }

    public LiveData<List<Label>> getLabels() { // New getter for labels
        return labels;
    }

    public String getCurrentCategoryOrLabelId() {
        return currentCategoryOrLabelId;
    }

    // method that fetch the data
//    public void fetchEmails() {
//        isLoading.setValue(true);
//        mailRepository.getInbox(new MailRepository.InboxCallback() {
//            @Override
//            public void onSuccess(List<Email> emails) {
//                currentEmails.postValue(emails);
//                isLoading.postValue(false);
//                error.postValue(null); // Clear any previous errors
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue(errorMessage);
//                isLoading.postValue(false);
//            }
//        });
//    }

    public void fetchEmailsForCategoryOrLabel(String identifier) {
        Log.d("InboxViewModel", "fetchEmailsForCategoryOrLabel called for: " + identifier);
        currentCategoryOrLabelId = identifier;
        isLoading.postValue(true); // הצג טעינה
        // Check if we are already displaying the current category/label
//        if (identifier.equals(currentCategoryOrLabelId) && currentEmails.getValue() != null) {
//            Log.d("InboxViewModel", "Already displaying " + identifier + ". Skipping refresh.");
//            isLoading.postValue(false); // Ensure loading is finished if no refresh is needed
//            return;
//        }

        currentCategoryOrLabelId = identifier; // Save the current identifier
        isLoading.setValue(true);
        error.setValue(null); // clear previous errors

        // MailRepository.InboxCallback callback = new MailRepository.InboxCallback() {
        MailRepository.ListEmailsCallback callback = new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                Log.d("InboxViewModel", "fetchEmailsForCategoryOrLabel onSuccess for " + identifier + ". Received " + (emails != null ? emails.size() : 0) + " emails.");
                currentEmails.postValue(emails);
                isLoading.postValue(false);
                error.postValue(null);
            }

            @Override
            public void onFailure(String errorMessage) {
                Log.e("InboxViewModel", "fetchEmailsForCategoryOrLabel onFailure for " + identifier + ": " + errorMessage);
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
                // הוסף כאן מקרים נוספים לקטגוריות מובנות
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

    // ממשק פונקציונלי פשוט ללוגיקת עדכון מייל
    private interface EmailUpdater {
        void update(Email email);
    }

    public void deleteEmail(String emailId) {
        mailRepository.deleteMail(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Email deleted: " + emailId);
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

//    public void markEmailAsRead(String emailId) {
//        mailRepository.markAsRead(emailId, new MailActionCallback() {
//            @Override
//            public void onSuccess() {
//                Log.d("InboxViewModel", "Email marked as read: " + emailId);
//                fetchEmails(); // Refresh after action
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue("Failed to mark as read: " + errorMessage);
//            }
//        });
//    }

    public void markEmailAsRead(String emailId) {
        mailRepository.markAsRead(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Email marked as read: " + emailId);
                updateEmailStatusInList(emailId, email -> email.setIsRead(true));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as read: " + errorMessage);
            }
        });
    }

//    public void markEmailAsUnread(String emailId) {
//        mailRepository.markAsUnread(emailId, new MailActionCallback() {
//            @Override
//            public void onSuccess() {
//                Log.d("InboxViewModel", "Email marked as unread: " + emailId);
//                fetchEmails(); // Refresh after action
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue("Failed to mark as unread: " + errorMessage);
//            }
//        });
//    }

    public void markEmailAsUnread(String emailId) {
        mailRepository.markAsUnread(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Email marked as unread: " + emailId);
                updateEmailStatusInList(emailId, email -> email.setIsRead(false));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as unread: " + errorMessage);
            }
        });
    }
    public void addMailToLabel(String emailId, String labelId) {
        mailRepository.addMailToLabel(emailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Mail " + emailId + " added to label " + labelId);
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to add mail to label: " + errorMessage);
            }
        });
    }

    public void addLabelToEmail(String emailId, String labelId) {
        mailRepository.addLabelToMail(emailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Label " + labelId + " added to email " + emailId);
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to add label: " + errorMessage);
            }
        });
    }

    public void markEmailAsImportant(String emailId) {
        mailRepository.markMailAsImportant(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Email marked as important: " + emailId);
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
                Log.d("InboxViewModel", "Email unmarked as important: " + emailId);
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
                Log.d("InboxViewModel", "Email marked as spam: " + emailId);
                List<Email> currentEmailsList = currentEmails.getValue();
                if (currentEmailsList != null) {
                    List<Email> updatedList = new java.util.ArrayList<>(currentEmailsList);
                    updatedList.removeIf(email -> email.getId().equals(emailId));
                    currentEmails.postValue(updatedList); // Update the LiveData immediately
                }
                // updateEmailStatusInList(emailId, email -> email.setSpam(true));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as spam: " + errorMessage);
            }
        });
    }

    public void unmarkEmailAsSpam(String emailId) {
        mailRepository.unmarkMailAsSpam(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Email unmarked as spam: " + emailId);
                updateEmailStatusInList(emailId, email -> email.setSpam(false));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to unmark as spam: " + errorMessage);
            }
        });
    }

    public void markEmailAsStarred(String emailId) {
        mailRepository.markMailAsStarred(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Email marked as starred: " + updatedEmailId);
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
                Log.d("InboxViewModel", "Email unmarked as starred: " + emailId);
                updateEmailStatusInList(emailId, email -> email.setStarred(false));
                fetchEmailsForCategoryOrLabel(currentCategoryOrLabelId);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to unmark as starred: " + errorMessage);
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

    