package com.example.android_app.data.network;

import com.example.android_app.model.Email;
import com.example.android_app.model.Label;
import com.example.android_app.model.LabelCreateRequest;
import com.example.android_app.model.LabelUpdateRequest;
import com.example.android_app.model.MailLabelRequest;


import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// This service class handles network operations related to labels in the Gmail application.
// It provides methods to get, create, update, delete labels, and manage emails associated with labels.
public class LabelService {

    private final ApiService api;

    public LabelService() {
        this.api = ApiClient.getClient().create(ApiService.class);
    }

    public interface LabelServiceCallback<T> {
        void onSuccess(T result);
        void onFailure(String errorMessage);
    }

// This method retrieves all labels for the authenticated user.
// It requires a valid authentication token and returns a list of labels or an error message.
    public void getLabels(String token, final LabelServiceCallback<List<Label>> callback) {
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }
        api.getLabels("Bearer " + token).enqueue(new Callback<List<Label>>() {
            @Override
            public void onResponse(Call<List<Label>> call, Response<List<Label>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to get labels: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            System.err.println("Error parsing error body: " + e.getMessage());
                        }
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Label>> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

// This method creates a new label with the specified name.
    public void createLabel(String token, String name, final LabelServiceCallback<Label> callback) {
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }
        LabelCreateRequest requestBody = new LabelCreateRequest(name);
        api.createLabel("Bearer " + token, requestBody).enqueue(new Callback<Label>() {
            @Override
            public void onResponse(Call<Label> call, Response<Label> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to create label: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            System.err.println("Error parsing error body: " + e.getMessage());
                        }
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Label> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

// This method updates an existing label with a new name.
    public void updateLabel(String token, String id, String newName, final LabelServiceCallback<Label> callback) {
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }
        LabelUpdateRequest requestBody = new LabelUpdateRequest(newName);
        api.updateLabel("Bearer " + token, id, requestBody).enqueue(new Callback<Label>() {
            @Override
            public void onResponse(Call<Label> call, Response<Label> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to update label: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            System.err.println("Error parsing error body: " + e.getMessage());
                        }
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Label> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }
// This method deletes a label by its ID. 
// It requires a valid authentication token and returns success or an error message.
    public void deleteLabel(String token, String id, final LabelServiceCallback<Void> callback) {
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }
        api.deleteLabel("Bearer " + token, id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    String errorMsg = "Failed to delete label: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            System.err.println("Error parsing error body: " + e.getMessage());
                        }
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

// This method retrieves all emails associated with a specific label.
    public void getMailsByLabel(String token, String id, final LabelServiceCallback<List<Email>> callback) {
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }
        api.getMailsByLabel("Bearer " + token, id).enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(Call<List<Email>> call, Response<List<Email>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to get mails by label: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            System.err.println("Error parsing error body: " + e.getMessage());
                        }
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Email>> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

// This method adds an email to a specific label.
    public void addMailToLabel(String token, String labelId, String mailId, final LabelServiceCallback<ResponseBody> callback) {
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }
        MailLabelRequest requestBody = new MailLabelRequest(mailId);
        api.addMailToLabel("Bearer " + token, labelId, requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    String errorMsg = "Failed to add mail to label: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            System.err.println("Error parsing error body: " + e.getMessage());
                        }
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }

// This method removes an email from a specific label.
    public void removeMailFromLabel(String token, String labelId, String mailId, final LabelServiceCallback<Void> callback) {
        if (token == null || token.isEmpty()) {
            callback.onFailure("Authentication token is missing.");
            return;
        }
        MailLabelRequest requestBody = new MailLabelRequest(mailId);
        api.removeMailFromLabel("Bearer " + token, labelId, requestBody).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    String errorMsg = "Failed to remove mail from label: " + response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " - " + response.errorBody().string();
                        } catch (Exception e) {
                            System.err.println("Error parsing error body: " + e.getMessage());
                        }
                    }
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onFailure("Network error: " + t.getMessage());
            }
        });
    }
}
