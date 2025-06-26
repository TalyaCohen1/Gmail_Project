package com.example.android_app.model.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android_app.data.network.LabelService; // Still needed for LabelService.LabelServiceCallback
import com.example.android_app.data.repository.LabelRepository; // Import the LabelRepository
import com.example.android_app.model.Label;
import com.example.android_app.model.Mail; // Assuming you'll need this for 'getMailsByLabel'

import java.util.List;

import okhttp3.ResponseBody; // For addMailToLabel success response if needed

public class LabelViewModel extends AndroidViewModel {

    // Now, this refers to your LabelRepository
    private final LabelRepository repository;

    // LiveData for labels list
    private final MutableLiveData<List<Label>> _labels = new MutableLiveData<>();
    public LiveData<List<Label>> getLabels() {
        return _labels;
    }

    // LiveData for any error messages
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> getErrorMessage() {
        return _errorMessage;
    }

    // LiveData for loading state
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    // LiveData for mails associated with a specific label (if you have a dedicated screen for it)
    private final MutableLiveData<List<Mail>> _mailsForLabel = new MutableLiveData<>();
    public LiveData<List<Mail>> getMailsForLabel() {
        return _mailsForLabel;
    }


    public LabelViewModel(@NonNull Application application) {
        super(application);
        // Initialize your LabelRepository, passing the application context
        this.repository = new LabelRepository(application);
    }

    /**
     * Fetches all labels for the authenticated user.
     * Updates _labels LiveData on success, _errorMessage on failure.
     */
    public void fetchLabels() {
        _isLoading.setValue(true); // Indicate loading
        // Token retrieval and checking is now handled by the repository
        repository.getLabels(new LabelService.LabelServiceCallback<List<Label>>() {
            @Override
            public void onSuccess(List<Label> result) {
                _labels.setValue(result); // Update LiveData with new labels
                _isLoading.setValue(false); // End loading
                _errorMessage.setValue(null); // Clear any previous error
            }

            @Override
            public void onFailure(String errorMessage) {
                _errorMessage.setValue("Failed to fetch labels: " + errorMessage);
                _isLoading.setValue(false); // End loading
            }
        });
    }

    /**
     * Creates a new label with the given name.
     * On success, it refreshes the list of labels.
     *
     * @param name The name of the label to create.
     */
    public void createLabel(String name) {
        if (name == null || name.trim().isEmpty()) {
            _errorMessage.setValue("Label name cannot be empty.");
            return;
        }

        _isLoading.setValue(true);
        // Token retrieval and checking is now handled by the repository
        repository.createLabel(name, new LabelService.LabelServiceCallback<Label>() {
            @Override
            public void onSuccess(Label result) {
                // If creation is successful, re-fetch the entire list to update the UI
                fetchLabels();
                // Optionally, show a success message
                _errorMessage.setValue("Label '" + result.getName() + "' created successfully.");
            }

            @Override
            public void onFailure(String errorMessage) {
                _errorMessage.setValue("Failed to create label: " + errorMessage);
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Updates an existing label's name.
     * On success, it refreshes the list of labels.
     *
     * @param labelId The ID of the label to update.
     * @param newName The new name for the label.
     */
    public void updateLabel(String labelId, String newName) {
        if (labelId == null || labelId.isEmpty()) {
            _errorMessage.setValue("Label ID is required for update.");
            return;
        }
        if (newName == null || newName.trim().isEmpty()) {
            _errorMessage.setValue("New label name cannot be empty.");
            return;
        }

        _isLoading.setValue(true);
        // Token retrieval and checking is now handled by the repository
        repository.updateLabel(labelId, newName, new LabelService.LabelServiceCallback<Label>() {
            @Override
            public void onSuccess(Label result) {
                fetchLabels(); // Refresh the list
                _errorMessage.setValue("Label updated to '" + result.getName() + "' successfully.");
            }

            @Override
            public void onFailure(String errorMessage) {
                _errorMessage.setValue("Failed to update label: " + errorMessage);
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Deletes a label.
     * On success, it refreshes the list of labels.
     *
     * @param labelId The ID of the label to delete.
     */
    public void deleteLabel(String labelId) {
        if (labelId == null || labelId.isEmpty()) {
            _errorMessage.setValue("Label ID is required for deletion.");
            return;
        }

        _isLoading.setValue(true);
        // Token retrieval and checking is now handled by the repository
        repository.deleteLabel(labelId, new LabelService.LabelServiceCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                fetchLabels(); // Refresh the list
                _errorMessage.setValue("Label deleted successfully.");
            }

            @Override
            public void onFailure(String errorMessage) {
                _errorMessage.setValue("Failed to delete label: " + errorMessage);
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Fetches mails associated with a specific label.
     * Updates _mailsForLabel LiveData on success, _errorMessage on failure.
     *
     * @param labelId The ID of the label whose mails are to be fetched.
     */
    public void fetchMailsForLabel(String labelId) {
        if (labelId == null || labelId.isEmpty()) {
            _errorMessage.setValue("Label ID is required to fetch mails.");
            return;
        }

        _isLoading.setValue(true);
        // Token retrieval and checking is now handled by the repository
        repository.getMailsByLabel(labelId, new LabelService.LabelServiceCallback<List<Mail>>() {
            @Override
            public void onSuccess(List<Mail> result) {
                _mailsForLabel.setValue(result); // Update LiveData with mails
                _isLoading.setValue(false);
                _errorMessage.setValue(null);
            }

            @Override
            public void onFailure(String errorMessage) {
                _errorMessage.setValue("Failed to fetch mails for label: " + errorMessage);
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Adds a mail to a label.
     * On success, it refreshes the mails for that label.
     *
     * @param labelId The ID of the label.
     * @param mailId  The ID of the mail to add.
     */
    public void addMailToLabel(String labelId, String mailId) {
        if (labelId == null || labelId.isEmpty() || mailId == null || mailId.isEmpty()) {
            _errorMessage.setValue("Label ID and Mail ID are required.");
            return;
        }

        _isLoading.setValue(true);
        // Token retrieval and checking is now handled by the repository
        repository.addMailToLabel(labelId, mailId, new LabelService.LabelServiceCallback<ResponseBody>() {
            @Override
            public void onSuccess(ResponseBody result) {
                fetchMailsForLabel(labelId); // Refresh the mails for this label
                _errorMessage.setValue("Mail added to label successfully.");
            }

            @Override
            public void onFailure(String errorMessage) {
                _errorMessage.setValue("Failed to add mail to label: " + errorMessage);
                _isLoading.setValue(false);
            }
        });
    }

    /**
     * Removes a mail from a label.
     * On success, it refreshes the mails for that label.
     *
     * @param labelId The ID of the label.
     * @param mailId  The ID of the mail to remove.
     */
    public void removeMailFromLabel(String labelId, String mailId) {
        if (labelId == null || labelId.isEmpty() || mailId == null || mailId.isEmpty()) {
            _errorMessage.setValue("Label ID and Mail ID are required.");
            return;
        }

        _isLoading.setValue(true);
        // Token retrieval and checking is now handled by the repository
        repository.removeMailFromLabel(labelId, mailId, new LabelService.LabelServiceCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                fetchMailsForLabel(labelId); // Refresh the mails for this label
                _errorMessage.setValue("Mail removed from label successfully.");
            }

            @Override
            public void onFailure(String errorMessage) {
                _errorMessage.setValue("Failed to remove mail from label: " + errorMessage);
                _isLoading.setValue(false);
            }
        });
    }
}
