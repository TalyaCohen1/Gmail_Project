package com.example.android_app.data.repository;

import android.content.Context; // Required for SharedPrefsManager
import com.example.android_app.data.network.LabelService;
import com.example.android_app.model.Label;
import com.example.android_app.model.Mail;
import com.example.android_app.utils.SharedPrefsManager;

import java.util.List;

import okhttp3.ResponseBody; // For addMailToLabel success response if needed

public class LabelRepository {

    private final LabelService labelService;
    private final Context context; // Context to get token from SharedPrefsManager or AuthManager

    public LabelRepository(Context context) {
        this.labelService = new LabelService(); // Initialize the network service
        this.context = context.getApplicationContext(); // Use application context to avoid leaks
    }

    // Helper to get the token (similar to your MailRepository)
    private String getToken(Context context) {
        return SharedPrefsManager.get(context, "token");
    }

    /**
     * Retrieves all labels for the authenticated user.
     * Delegates to LabelService and passes the result/error via callback.
     *
     * @param callback The callback to handle the list of labels or an error.
     */
    public void getLabels(final LabelService.LabelServiceCallback<List<Label>> callback) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token not found. Please log in.");
            return;
        }
        labelService.getLabels(token, callback);
    }

    /**
     * Creates a new label.
     *
     * @param name     The name of the new label.
     * @param callback The callback to handle the created label or an error.
     */
    public void createLabel(String name, final LabelService.LabelServiceCallback<Label> callback) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token not found. Please log in.");
            return;
        }
        labelService.createLabel(token, name, callback);
    }

    /**
     * Updates an existing label's name.
     *
     * @param labelId The ID of the label to update.
     * @param newName The new name for the label.
     * @param callback The callback to handle the updated label or an error.
     */
    public void updateLabel(String labelId, String newName, final LabelService.LabelServiceCallback<Label> callback) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token not found. Please log in.");
            return;
        }
        labelService.updateLabel(token, labelId, newName, callback);
    }

    /**
     * Deletes a label.
     *
     * @param labelId The ID of the label to delete.
     * @param callback The callback to handle success or failure.
     */
    public void deleteLabel(String labelId, final LabelService.LabelServiceCallback<Void> callback) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token not found. Please log in.");
            return;
        }
        labelService.deleteLabel(token, labelId, callback);
    }

    /**
     * Fetches mails associated with a specific label.
     *
     * @param labelId The ID of the label whose mails are to be fetched.
     * @param callback The callback to handle the list of mails or an error.
     */
    public void getMailsByLabel(String labelId, final LabelService.LabelServiceCallback<List<Mail>> callback) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token not found. Please log in.");
            return;
        }
        labelService.getMailsByLabel(token, labelId, callback);
    }

    /**
     * Adds a mail to a label.
     *
     * @param labelId The ID of the label.
     * @param mailId  The ID of the mail to add.
     * @param callback The callback to handle success or failure.
     */
    public void addMailToLabel(String labelId, String mailId, final LabelService.LabelServiceCallback<ResponseBody> callback) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token not found. Please log in.");
            return;
        }
        labelService.addMailToLabel(token, labelId, mailId, callback);
    }

    /**
     * Removes a mail from a label.
     *
     * @param labelId The ID of the label.
     * @param mailId  The ID of the mail to remove.
     * @param callback The callback to handle success or failure.
     */
    public void removeMailFromLabel(String labelId, String mailId, final LabelService.LabelServiceCallback<Void> callback) {
        String token = getToken(context);
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token not found. Please log in.");
            return;
        }
        labelService.removeMailFromLabel(token, labelId, mailId, callback);
    }
}
