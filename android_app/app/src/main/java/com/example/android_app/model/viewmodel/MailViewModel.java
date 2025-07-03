// MailViewModel.java

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
import com.example.android_app.utils.SharedPrefsManager;
import com.example.android_app.data.network.MailService;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailViewModel extends AndroidViewModel {

    private static final String TAG = "MailViewModel";

    private final MailRepository mailRepository;

    public MailViewModel(@NonNull Application application) {
        super(application);
        this.mailRepository = new MailRepository(application.getApplicationContext());
        // Initialize counts map when ViewModel is created
        initializeMailCounts();
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

    private final MutableLiveData<List<Email>> _allMails = new MutableLiveData<>();
    public LiveData<List<Email>> getAllMails() {
        return _allMails;
    }

    private final MutableLiveData<Email> _selectedMailDetails = new MutableLiveData<>();
    public LiveData<Email> getSelectedMailDetails() {
        return _selectedMailDetails;
    }

    private final MutableLiveData<List<Label>> _mailLabels = new MutableLiveData<>();
    public LiveData<List<Label>> getMailLabels() {
        return _mailLabels;
    }

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

    // New LiveData for mail counts for categories and labels
    private final MutableLiveData<Map<String, Integer>> _mailCounts = new MutableLiveData<>();
    public LiveData<Map<String, Integer>> getMailCounts() {
        return _mailCounts;
    }

    // A map to hold the current counts, initialized to 0
    private Map<String, Integer> currentMailCounts = new HashMap<>();


    private void initializeMailCounts() {
        // Initialize common categories
        currentMailCounts.put("inbox", 0);
        currentMailCounts.put("sent", 0);
        currentMailCounts.put("drafts", 0);
        currentMailCounts.put("spam", 0);
        currentMailCounts.put("deleted", 0);
        currentMailCounts.put("important", 0);
        currentMailCounts.put("starred", 0);
        _mailCounts.postValue(currentMailCounts); // Set initial values
    }

    /**
     * Fetches mail counts for all standard categories and updates _mailCounts LiveData.
     */
    public void fetchAllCategoryCounts() {
        _isLoading.postValue(true);

        // Use a counter or a latch if you need to know when all fetches are complete
        // For simplicity, we'll update as each one completes.
        // In a real app, consider using a CompletableFuture or RxJava for better coordination.

        // Fetch Inbox count
        mailRepository.getInbox(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                currentMailCounts.put("inbox", emails.size());
                _mailCounts.postValue(currentMailCounts); // Update LiveData
                Log.d(TAG, "Fetched Inbox count: " + emails.size());
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to fetch Inbox count: " + error);
                _errorMessage.postValue(error);
                checkAllCountsFetched();
            }
        });

        // Fetch Sent count
        mailRepository.getSentMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                currentMailCounts.put("sent", emails.size());
                _mailCounts.postValue(currentMailCounts);
                Log.d(TAG, "Fetched Sent count: " + emails.size());
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to fetch Sent count: " + error);
                _errorMessage.postValue(error);
                checkAllCountsFetched();
            }
        });

        // Fetch Drafts count
        mailRepository.getDrafts(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                currentMailCounts.put("drafts", emails.size());
                _mailCounts.postValue(currentMailCounts);
                Log.d(TAG, "Fetched Drafts count: " + emails.size());
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to fetch Drafts count: " + error);
                _errorMessage.postValue(error);
                checkAllCountsFetched();
            }
        });

        // Fetch Spam count
        mailRepository.getSpamMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                currentMailCounts.put("spam", emails.size());
                _mailCounts.postValue(currentMailCounts);
                Log.d(TAG, "Fetched Spam count: " + emails.size());
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to fetch Spam count: " + error);
                _errorMessage.postValue(error);
                checkAllCountsFetched();
            }
        });

        // Fetch Deleted count
        mailRepository.getDeletedMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                currentMailCounts.put("deleted", emails.size());
                _mailCounts.postValue(currentMailCounts);
                Log.d(TAG, "Fetched Deleted count: " + emails.size());
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to fetch Deleted count: " + error);
                _errorMessage.postValue(error);
                checkAllCountsFetched();
            }
        });

        // Fetch Important count
        mailRepository.getImportantMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                currentMailCounts.put("important", emails.size());
                _mailCounts.postValue(currentMailCounts);
                Log.d(TAG, "Fetched Important count: " + emails.size());
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to fetch Important count: " + error);
                _errorMessage.postValue(error);
                checkAllCountsFetched();
            }
        });

        // Fetch Starred count
        mailRepository.getStarredMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                currentMailCounts.put("starred", emails.size());
                _mailCounts.postValue(currentMailCounts);
                Log.d(TAG, "Fetched Starred count: " + emails.size());
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to fetch Starred count: " + error);
                _errorMessage.postValue(error);
                checkAllCountsFetched();
            }
        });
    }

    private int completedCountFetches = 0;
    private final int TOTAL_CATEGORY_COUNTS = 7; // Inbox, Sent, Drafts, Spam, Deleted, Important, Starred

    private void checkAllCountsFetched() {
        completedCountFetches++;
        if (completedCountFetches == TOTAL_CATEGORY_COUNTS) {
            _isLoading.postValue(false);
            completedCountFetches = 0; // Reset for next fetch
        }
    }


    /**
     * Fetches mail counts for a specific label and updates _mailCounts LiveData.
     * This method is intended to be called for each custom label.
     *
     * @param labelId The ID of the label.
     * @param labelName The name of the label (to be used as key in the map).
     */
    public void fetchMailCountForLabel(String labelId, String labelName) {
        if (labelId == null || labelId.isEmpty() || labelName == null || labelName.isEmpty()) {
            _errorMessage.postValue("Label ID and Label Name are required to fetch mail count for label.");
            return;
        }

        // No need to set overall _isLoading to true here, as fetchAllCategoryCounts handles main loading.
        // This is a supplementary fetch for a specific label.

        mailRepository.getMailsByLabel(labelId, new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                // Update the count for this specific label
                currentMailCounts.put(labelName, emails.size());
                _mailCounts.postValue(currentMailCounts); // Trigger LiveData update
                Log.d(TAG, "Fetched count for label '" + labelName + "': " + emails.size());
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Failed to fetch mail count for label '" + labelName + "': " + error);
                _errorMessage.postValue(error);
            }
        });
    }

    // --- Mail Fetching Operations ---
    public void fetchInboxMails() {
        _isLoading.postValue(true);
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


    public void fetchAllMails() { 
        _isLoading.postValue(true);
        mailRepository.listMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _allMails.postValue(emails);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Fetched All Mails: " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to fetch All Mails: " + error);
            }
        });
    }

    public void fetchSentMails() {

        _isLoading.postValue(true);
        mailRepository.getSentMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _sentMails.postValue(emails);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Fetched Sent: " + emails.size() + " mails");
                // Also update count
                currentMailCounts.put("sent", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to fetch Sent: " + error);
            }
        });
    }

    public void fetchDrafts() { 
        _isLoading.postValue(true);
        mailRepository.getDrafts(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _drafts.postValue(emails);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Fetched Drafts: " + emails.size() + " mails");
                // Also update count
                currentMailCounts.put("drafts", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to fetch Drafts: " + error);
            }
        });
    }

    public void fetchSpamMails() { 
        _isLoading.postValue(true);
        mailRepository.getSpamMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _spamMails.postValue(emails);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Fetched Spam: " + emails.size() + " mails");
                // Also update count
                currentMailCounts.put("spam", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to fetch Spam: " + error);
            }
        });
    }

    public void fetchDeletedMails() { 
        _isLoading.postValue(true);
        mailRepository.getDeletedMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _deletedMails.postValue(emails);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Fetched Deleted: " + emails.size() + " mails");
                // Also update count
                currentMailCounts.put("deleted", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to fetch Deleted: " + error);
            }
        });
    }

    public void fetchImportantMails() { 
        _isLoading.postValue(true);
        mailRepository.getImportantMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _importantMails.postValue(emails);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Fetched Important: " + emails.size() + " mails");
                // Also update count
                currentMailCounts.put("important", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to fetch Important: " + error);
            }
        });
    }

    public void fetchStarredMails() { 
        _isLoading.postValue(true);
        mailRepository.getStarredMails(new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _starredMails.postValue(emails);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Fetched Starred: " + emails.size() + " mails");
                // Also update count
                currentMailCounts.put("starred", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to fetch Starred: " + error);
            }
        });
    }

    public void searchMails(String query) { 
        _isLoading.postValue(true);
        mailRepository.searchMails(query, new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _searchResults.postValue(emails);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Search Results: " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to search mails: " + error);
            }
        });
    }

    public void fetchMailDetails(String mailId) { 
        _isLoading.postValue(true);
        mailRepository.getEmailById(mailId, new MailRepository.EmailDetailsCallback() {
            @Override
            public void onSuccess(Email email) {
                _selectedMailDetails.postValue(email);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Fetched Mail Details for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to fetch mail details for ID " + mailId + ": " + error);
            }
        });
    }

    public void fetchMailsByLabel(String labelId) { 
        _isLoading.postValue(true);
        mailRepository.getMailsByLabel(labelId, new MailRepository.ListEmailsCallback() {
            @Override
            public void onSuccess(List<Email> emails) {
                _mailsByLabel.postValue(emails);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Fetched Mails by Label " + labelId + ": " + emails.size() + " mails");
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to fetch mails by label " + labelId + ": " + error);
            }
        });
    }

    public void deleteMail(String mailId) {
        _isLoading.postValue(true);
        mailRepository.deleteMail(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Mail deleted successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to delete mail for ID " + mailId + ": " + error);
            }
        });
    }

    public void markMailAsSpam(String mailId) {
        _isLoading.postValue(true);
        mailRepository.markMailAsSpam(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Mail marked as spam successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to mark mail as spam for ID " + mailId + ": " + error);
            }
        });
    }

    public void unmarkMailAsSpam(String mailId) {
        _isLoading.postValue(true);
        mailRepository.unmarkMailAsSpam(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Mail unmarked as spam successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to unmark mail as spam for ID " + mailId + ": " + error);
            }
        });
    }
  public void markMailAsImportant(String mailId) {
        _isLoading.postValue(true);
        mailRepository.markMailAsImportant(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Mail marked as important successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to mark mail as important for ID " + mailId + ": " + error);
            }
        });
    }

    public void unmarkMailAsImportant(String mailId) {
        _isLoading.postValue(true);
        mailRepository.unmarkMailAsImportant(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Mail unmarked as important successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to unmark mail as important for ID " + mailId + ": " + error);
            }
        });
    }

    public void markMailAsStarred(String mailId) {
        _isLoading.postValue(true);
        mailRepository.markMailAsStarred(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Mail marked as starred successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to mark mail as starred for ID " + mailId + ": " + error);
            }
        });
    }

    public void unmarkMailAsStarred(String mailId) {
        _isLoading.postValue(true);
        mailRepository.unmarkMailAsStarred(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Mail unmarked as starred successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to unmark mail as starred for ID " + mailId + ": " + error);
            }
        });
    }

    public void markAsRead(String mailId) {
        _isLoading.postValue(true);
        mailRepository.markAsRead(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Mail marked as read successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to mark mail as read for ID " + mailId + ": " + error);
            }
        });
    }

    public void markAsUnread(String mailId) {
        _isLoading.postValue(true);
        mailRepository.markAsUnread(mailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Mail marked as unread successfully for ID: " + mailId);
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to mark mail as unread for ID " + mailId + ": " + error);
            }
        });
    }
    public void addLabelToMail(String mailId, String labelId) {
        _isLoading.postValue(true);
        mailRepository.addLabelToMail(mailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Label " + labelId + " added to mail " + mailId + " successfully.");
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to add label " + labelId + " to mail " + mailId + ": " + error);
            }
        });
    }

    public void removeLabelFromMail(String mailId, String labelId) {
        _isLoading.postValue(true);
        mailRepository.removeLabelFromMail(mailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String mailId) {
                _actionSuccess.postValue(true);
                _isLoading.postValue(false);
                _errorMessage.postValue(null);
                Log.d(TAG, "Label " + labelId + " removed from mail " + mailId + " successfully.");
            }

            @Override
            public void onFailure(String error) {
                _actionSuccess.postValue(false);
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
                Log.e(TAG, "Failed to remove label " + labelId + " from mail " + mailId + ": " + error);
            }
        });
    }

    /**
     * Resets the action success LiveData. Call this after consuming the success state in UI.
     */
    public void resetActionSuccess() {
        _actionSuccess.postValue(false);
    }
}