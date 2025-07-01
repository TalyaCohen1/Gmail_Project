package com.example.android_app.model.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android_app.data.repository.MailRepository;
import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest;
import com.example.android_app.model.Label;
import com.example.android_app.utils.SendCallback;

import java.util.List;

public class MailViewModel extends AndroidViewModel {

    private static final String TAG = "MailViewModel";

    private final MailRepository mailRepository;
    public MailViewModel(@NonNull Application application) {
        super(application);
        // Initialize MailRepository using the application context
        this.mailRepository = new MailRepository(application.getApplicationContext());
    }

    // LiveData for various mail lists
    private final MutableLiveData<List<Email>> _inboxMails = new MutableLiveData<>();
    public LiveData<List<Email>> getInboxMails() {
        return _inboxMails;
    }

    private final MutableLiveData<List<Email>> _sentMails = new MutableLiveData<>();
    public LiveData<List<Email>> getSentMails() {
        return _sentMails;
    }

    private final MutableLiveData<List<Email>> _drafts = new MutableLiveData<>();
    public LiveData<List<Email>> getDrafts() {
        return _drafts;
    }

    private final MutableLiveData<List<Email>> _spamMails = new MutableLiveData<>();
    public LiveData<List<Email>> getSpamMails() {
        return _spamMails;
    }

    private final MutableLiveData<List<Email>> _deletedMails = new MutableLiveData<>();
    public LiveData<List<Email>> getDeletedMails() {
        return _deletedMails;
    }

    private final MutableLiveData<List<Email>> _importantMails = new MutableLiveData<>();
    public LiveData<List<Email>> getImportantMails() {
        return _importantMails;
    }

    private final MutableLiveData<List<Email>> _starredMails = new MutableLiveData<>();
    public LiveData<List<Email>> getStarredMails() {
        return _starredMails;
    }

    private final MutableLiveData<List<Email>> _searchResults = new MutableLiveData<>();
    public LiveData<List<Email>> getSearchResults() {
        return _searchResults;
    }

    private final MutableLiveData<List<Email>> _mailsByLabel = new MutableLiveData<>();
    public LiveData<List<Email>> getMailsByLabel() {
        return _mailsByLabel;
    }

    private final MutableLiveData<Email> _selectedMailDetails = new MutableLiveData<>();
    public LiveData<Email> getSelectedMailDetails() {
        return _selectedMailDetails;
    }

    private final MutableLiveData<List<Label>> _mailLabels = new MutableLiveData<>();
    public LiveData<List<Label>> getMailLabels() {
        return _mailLabels;
    }

    // LiveData for general actions feedback (success/failure)
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    private final MutableLiveData<Boolean> _actionSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getActionSuccess() {
        return _actionSuccess;
    }

    // --- Mail Fetching Operations ---

    public void fetchInboxMails() {
        _isLoading.setValue(true);
        mailRepository.fetchInboxAndSaveToLocal(new MailRepository.ActionCallback() {
            @Override
            public void onSuccess() {
                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
            }
        });
    }


    public void fetchSentMails() {
        _isLoading.setValue(true);
        mailRepository.getSentMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _sentMails.setValue(emails);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Fetched Sent: " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to fetch Sent: " + error);
            }
        });
    }

    public void fetchDrafts() {
        _isLoading.setValue(true);
        mailRepository.getDrafts(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _drafts.setValue(emails);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Fetched Drafts: " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to fetch Drafts: " + error);
            }
        });
    }

    public void fetchSpamMails() {
        _isLoading.setValue(true);
        mailRepository.getSpamMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _spamMails.setValue(emails);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Fetched Spam: " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to fetch Spam: " + error);
            }
        });
    }

    public void fetchDeletedMails() {
        _isLoading.setValue(true);
        mailRepository.getDeletedMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _deletedMails.setValue(emails);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Fetched Deleted: " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to fetch Deleted: " + error);
            }
        });
    }

    public void fetchImportantMails() {
        _isLoading.setValue(true);
        mailRepository.getImportantMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _importantMails.setValue(emails);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Fetched Important: " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to fetch Important: " + error);
            }
        });
    }

    public void fetchStarredMails() {
        _isLoading.setValue(true);
        mailRepository.getStarredMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _starredMails.setValue(emails);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Fetched Starred: " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to fetch Starred: " + error);
            }
        });
    }

    public void searchMails(String query) {
        _isLoading.setValue(true);
        // Corrected callback type to ListEmailsCallback
        mailRepository.searchMails(query, new MailRepository.ListEmailsCallback() { // Changed from ListLabelsCallback to ListEmailsCallback
            @Override
            public void onSuccess(List<Email> emails) {
                _searchResults.setValue(emails);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Search Results: " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to search mails: " + error);
            }
        });
    }

    public void fetchMailDetails(String mailId) {
        _isLoading.setValue(true);
        mailRepository.getEmailById(mailId, new MailRepository.EmailDetailsCallback() {
            @Override
            public void onSuccess(Email email) {
                _selectedMailDetails.setValue(email);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Fetched Mail Details for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to fetch mail details for ID " + mailId + ": " + error);
            }
        });
    }

    public void fetchMailsByLabel(String labelId) {
        _isLoading.setValue(true);
        // Corrected call to mailRepository.getMailsByLabel
        mailRepository.getMailsByLabel(labelId, new MailRepository.ListEmailsCallback() { // Changed to ListEmailsCallback
            @Override
            public void onSuccess(List<Email> emails) {
                _mailsByLabel.setValue(emails);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Fetched Mails by Label " + labelId + ": " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to fetch mails by label " + labelId + ": " + error);
            }
        });
    }


    // --- Mail Action Operations ---

    public void sendEmail(String to, String subject, String body) {
        _isLoading.setValue(true);
        mailRepository.sendEmail(to, subject, body, new SendCallback() {
            @Override
            public void onSuccess() {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Email sent successfully.");
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to send email: " + error);
            }
        });
    }

    public void updateDraft(String mailId, EmailRequest request) {
        _isLoading.setValue(true);
        mailRepository.updateDraft(mailId, request, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Draft updated successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to update draft for ID " + mailId + ": " + error);
            }
        });
    }

    public void deleteMail(String mailId) {
        _isLoading.setValue(true);
        mailRepository.deleteMail(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Mail deleted successfully for ID: " + mailId);
                // Optionally, refresh relevant mail lists after deletion
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to delete mail for ID " + mailId + ": " + error);
            }
        });
    }

    public void markMailAsSpam(String mailId) {
        _isLoading.setValue(true);
        mailRepository.markMailAsSpam(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Mail marked as spam successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to mark mail as spam for ID " + mailId + ": " + error);
            }
        });
    }

    public void unmarkMailAsSpam(String mailId) {
        _isLoading.setValue(true);
        mailRepository.unmarkMailAsSpam(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Mail unmarked as spam successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to unmark mail as spam for ID " + mailId + ": " + error);
            }
        });
    }

    public void markMailAsImportant(String mailId) {
        _isLoading.setValue(true);
        mailRepository.markMailAsImportant(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Mail marked as important successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to mark mail as important for ID " + mailId + ": " + error);
            }
        });
    }

    public void unmarkMailAsImportant(String mailId) {
        _isLoading.setValue(true);
        mailRepository.unmarkMailAsImportant(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Mail unmarked as important successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to unmark mail as important for ID " + mailId + ": " + error);
            }
        });
    }

    public void markMailAsStarred(String mailId) {
        _isLoading.setValue(true);
        mailRepository.markMailAsStarred(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Mail marked as starred successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to mark mail as starred for ID " + mailId + ": " + error);
            }
        });
    }

    public void unmarkMailAsStarred(String mailId) {
        _isLoading.setValue(true);
        mailRepository.unmarkMailAsStarred(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Mail unmarked as starred successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to unmark mail as starred for ID " + mailId + ": " + error);
            }
        });
    }

    public void markAsRead(String mailId) {
        _isLoading.setValue(true);
        mailRepository.markAsRead(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Mail marked as read successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to mark mail as read for ID " + mailId + ": " + error);
            }
        });
    }

    public void markAsUnread(String mailId) {
        _isLoading.setValue(true);
        mailRepository.markAsUnread(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Mail marked as unread successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to mark mail as unread for ID " + mailId + ": " + error);
            }
        });
    }

    public void addLabelToMail(String mailId, String labelId) {
        _isLoading.setValue(true);
        mailRepository.addLabelToMail(mailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Label " + labelId + " added to mail " + mailId + " successfully.");
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to add label " + labelId + " to mail " + mailId + ": " + error);
            }
        });
    }

    public void removeLabelFromMail(String mailId, String labelId) {
        _isLoading.setValue(true);
        mailRepository.removeLabelFromMail(mailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.setValue(true);
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
                Log.d(TAG, "Label " + labelId + " removed from mail " + mailId + " successfully.");
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.setValue(false);
                _errorMessage.setValue(error);
                _isLoading.setValue(false);
                Log.e(TAG, "Failed to remove label " + labelId + " from mail " + mailId + ": " + error);
            }
        });
    }

    /**
     * Resets the action success LiveData. Call this after consuming the success state in UI.
     */
    public void resetActionSuccess() {
        _actionSuccess.setValue(false);
    }
}