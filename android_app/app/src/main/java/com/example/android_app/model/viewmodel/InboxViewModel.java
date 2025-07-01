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
    private final MutableLiveData<List<Email>> inboxEmails = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<List<Label>> labels = new MutableLiveData<>(); // New: LiveData for labels

    public InboxViewModel(@NonNull Application application) {
        super(application);
        mailRepository = new MailRepository(application);
        fetchEmails();
        fetchLabels(); // Fetch labels when ViewModel is created
    }

//    public LiveData<List<MailEntity>> getInboxEmails() {
//        return mailRepository.getLocalInbox();
//    }
    public LiveData<List<Email>> getInboxEmails() {
        return inboxEmails;
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

    // method that fetch the data
    public void fetchEmails() {
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
                error.postValue(errorMessage);
                isLoading.postValue(false);
            }
        });
    }

    private void updateEmailStatusInList(String emailId, EmailUpdater updater) {
        List<Email> currentEmails = inboxEmails.getValue();
        if (currentEmails != null) {
            // יצירת רשימה חדשה כדי לא לשנות ישירות את הרשימה הקיימת (immutability)
            List<Email> updatedList = new java.util.ArrayList<>(currentEmails);
            boolean found = false;
            for (int i = 0; i < updatedList.size(); i++) {
                Email email = updatedList.get(i);
                if (email.getId().equals(emailId)) {
                    updater.update(email); // הפעל את הלוגיקה הספציפית לעדכון המייל
                    found = true;
                    break;
                }
            }
            if (found) {
                inboxEmails.postValue(updatedList); // עדכן את ה-LiveData כדי שה-UI יתעדכן
            }
        }
    }

    // ממשק פונקציונלי פשוט ללוגיקת עדכון מייל
    private interface EmailUpdater {
        void update(Email email);
    }


//    public void deleteEmail(String emailId) {
//        // Optionally show a loading state for this specific action
//        mailRepository.deleteMail(emailId, new MailActionCallback() {
//            @Override
//            public void onSuccess() {
//                Log.d("InboxViewModel", "Email deleted: " + emailId);
//                fetchEmails(); // Refresh the inbox after deletion
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue("Failed to delete email: " + errorMessage);
//            }
//        });
//    }
    public void deleteEmail(String emailId) {
        mailRepository.deleteMail(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Email deleted: " + emailId);
                List<Email> currentEmails = inboxEmails.getValue();
                if (currentEmails != null) {
                    List<Email> updatedList = new java.util.ArrayList<>(currentEmails);
                    updatedList.removeIf(email -> email.getId().equals(emailId));
                    inboxEmails.postValue(updatedList);
                }
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
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to mark as unread: " + errorMessage);
            }
        });
    }

//    public void addLabelToEmail(String emailId, String labelId) {
//        mailRepository.addLabelToMail(emailId, labelId, new MailActionCallback() {
//            @Override
//            public void onSuccess() {
//                Log.d("InboxViewModel", "Label " + labelId + " added to email " + emailId);
//                fetchEmails(); // Refresh after action
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue("Failed to add label: " + errorMessage);
//            }
//        });
//    }

    public void addLabelToEmail(String emailId, String labelId) {
        mailRepository.addLabelToMail(emailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Label " + labelId + " added to email " + emailId);
                // אתה צריך גם לעדכן את שדות ה-label במודל Email, אם אתה שומר אותם
                // updateEmailStatusInList(emailId, email -> email.addLabel(labelId)); // דורש שינוי במודל Email
                fetchEmails();
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to add label: " + errorMessage);
            }
        });
    }

//    public void markEmailAsImportant(String emailId) {
//        mailRepository.markMailAsImportant(emailId, new MailActionCallback() {
//            @Override
//            public void onSuccess() {
//                Log.d("InboxViewModel", "Email marked as important: " + emailId);
//                fetchEmails();
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue("Failed to mark as important: " + errorMessage);
//            }
//        });
//    }
//
//    public void unmarkEmailAsImportant(String emailId) {
//        mailRepository.unmarkMailAsImportant(emailId, new MailActionCallback() { // תצטרך ליישם את unmarkMailAsImportant ב-MailRepository
//            @Override
//            public void onSuccess() {
//                Log.d("InboxViewModel", "Email unmarked as important: " + emailId);
//                fetchEmails();
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue("Failed to unmark as important: " + errorMessage);
//            }
//        });
//    }
//
//    public void markEmailAsSpam(String emailId) {
//        mailRepository.markMailAsSpam(emailId, new MailActionCallback() {
//            @Override
//            public void onSuccess() {
//                fetchEmails();
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue("Failed to mark as spam: " + errorMessage);
//            }
//        });
//    }
//
//    public void unmarkEmailAsSpam(String emailId) {
//        mailRepository.unmarkMailAsSpam(emailId, new MailActionCallback() { // תצטרך ליישם את unmarkMailAsSpam ב-MailRepository
//            @Override
//            public void onSuccess() {
//                Log.d("InboxViewModel", "Email unmarked as spam: " + emailId);
//                fetchEmails(); // רענן את התיבה לאחר הפעולה
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue("Failed to unmark as spam: " + errorMessage);
//            }
//        });
//    }
//
//    public void markEmailAsStarred(String emailId) {
//        mailRepository.markMailAsStarred(emailId, new MailActionCallback() { // תצטרך ליישם את markMailAsStarred ב-MailRepository
//            @Override
//            public void onSuccess() {
//                Log.d("InboxViewModel", "Email marked as starred: " + emailId);
//                fetchEmails(); // רענן את התיבה לאחר הפעולה
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue("Failed to mark as starred: " + errorMessage);
//            }
//        });
//    }
//
//    public void unmarkEmailAsStarred(String emailId) {
//        mailRepository.unmarkMailAsStarred(emailId, new MailActionCallback() {
//            @Override
//            public void onSuccess() {
//                Log.d("InboxViewModel", "Email unmarked as starred: " + emailId);
//                fetchEmails(); // רענן את התיבה לאחר הפעולה
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                error.postValue("Failed to unmark as starred: " + errorMessage);
//            }
//        });
//    }

    public void markEmailAsImportant(String emailId) {
        mailRepository.markMailAsImportant(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String updatedEmailId) {
                Log.d("InboxViewModel", "Email marked as important: " + emailId);
                updateEmailStatusInList(emailId, email -> email.setImportant(true));
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
                updateEmailStatusInList(emailId, email -> email.setSpam(true));
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