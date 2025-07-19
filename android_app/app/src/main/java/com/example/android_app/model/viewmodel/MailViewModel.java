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
import com.example.android_app.model.Label;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailViewModel extends AndroidViewModel {

    private static final String TAG = "MailViewModel";

    private final MailRepository mailRepository;

    public MailViewModel(@NonNull Application application) {
        super(application);
        this.mailRepository = new MailRepository(application.getApplicationContext());
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
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
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
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
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
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
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
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
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
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
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
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
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
                checkAllCountsFetched();
            }

            @Override
            public void onFailure(String error) {
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
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
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
                // Also update count
                currentMailCounts.put("sent", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
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
                // Also update count
                currentMailCounts.put("drafts", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
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
                // Also update count
                currentMailCounts.put("spam", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
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
                // Also update count
                currentMailCounts.put("deleted", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
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
                // Also update count
                currentMailCounts.put("important", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
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
                // Also update count
                currentMailCounts.put("starred", emails.size());
                _mailCounts.postValue(currentMailCounts);
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
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
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
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
            }

            @Override
            public void onFailure(String error) {
                _errorMessage.postValue(error);
                _isLoading.postValue(false);
            }
        });
    }
}