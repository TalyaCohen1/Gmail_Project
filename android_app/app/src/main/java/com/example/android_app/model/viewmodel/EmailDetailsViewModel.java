package com.example.android_app.model.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android_app.data.repository.MailRepository;
import com.example.android_app.model.Email;
import com.example.android_app.model.Label;

import java.util.List;
// This ViewModel is responsible for managing the email details and actions related to a specific email.
// It interacts with the MailRepository to perform actions like marking an email as important, starred,
public class EmailDetailsViewModel extends AndroidViewModel {

    private final MailRepository mailRepository;
    private final MutableLiveData<Email> emailDetails = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<Label>> allLabels = new MutableLiveData<>();

    public EmailDetailsViewModel(@NonNull Application application) {
        super(application);
        mailRepository = new MailRepository(application);
    }

    public LiveData<Email> getEmailDetails() {
        return emailDetails;
    }

    public LiveData<String> getError() {
        return error;
    }
    public LiveData<List<Label>> getAllLabels() {
        return allLabels;
    }

// This method toggles the important status of an email.
// If the email is marked as important, it calls the repository method to mark it as important
    public void toggleMailImportant(String emailId, boolean isImportant) {
        if (isImportant) {
            mailRepository.markMailAsImportant(emailId, new MailRepository.MailActionCallback() {
                @Override
                public void onSuccess(String id) {
                    fetchEmailById(id);
                }
                @Override
                public void onFailure(String errorMessage) {
                    error.postValue(errorMessage);
                }
            });
        } else {
            mailRepository.unmarkMailAsImportant(emailId, new MailRepository.MailActionCallback() {
                @Override
                public void onSuccess(String id) {
                    fetchEmailById(id);
                }
                @Override
                public void onFailure(String errorMessage) {
                    error.postValue(errorMessage);
                }
            });
        }
    }

// This method toggles the starred status of an email.
// If the email is starred, it calls the repository method to mark it as starred, otherwise
    public void toggleMailStarred(String emailId, boolean isStarred) {
        if (isStarred) {
            mailRepository.markMailAsStarred(emailId, new MailRepository.MailActionCallback() {
                @Override
                public void onSuccess(String id) {
                    fetchEmailById(id);
                }
                @Override
                public void onFailure(String errorMessage) {
                    error.postValue(errorMessage);
                }
            });
        } else {
            mailRepository.unmarkMailAsStarred(emailId, new MailRepository.MailActionCallback() {
                @Override
                public void onSuccess(String id) {
                    fetchEmailById(id);
                }
                @Override
                public void onFailure(String errorMessage) {
                    error.postValue(errorMessage);
                }
            });
        }
    }

// This method toggles the read status of an email.
// If the email is marked as read, it calls the repository method to mark it as read
    public void toggleMailReadStatus(String emailId, boolean isRead) {
        if (isRead) {
            mailRepository.markAsRead(emailId, new MailRepository.MailActionCallback() {
                @Override
                public void onSuccess(String id) {
                    fetchEmailById(id);
                }
                @Override
                public void onFailure(String errorMessage) {
                    error.postValue(errorMessage);
                }
            });
        } else {
            mailRepository.markAsUnread(emailId, new MailRepository.MailActionCallback() {
                @Override
                public void onSuccess(String id) {
                    fetchEmailById(id);
                }
                @Override
                public void onFailure(String errorMessage) {
                    error.postValue(errorMessage);
                }
            });
        }
    }

// This method toggles the spam status of an email.
// If the email is marked as spam, it calls the repository method to mark it as spam
    public void toggleMailSpam(String emailId, boolean isSpam) {
        if (isSpam) {
            mailRepository.markMailAsSpam(emailId, new MailRepository.MailActionCallback() {
                @Override
                public void onSuccess(String id) {
                    fetchEmailById(id);
                }
                @Override
                public void onFailure(String errorMessage) {
                    error.postValue(errorMessage);
                }
            });
        } else {
            mailRepository.unmarkMailAsSpam(emailId, new MailRepository.MailActionCallback() {
                @Override
                public void onSuccess(String id) {
                    fetchEmailById(id);
                }
                @Override
                public void onFailure(String errorMessage) {
                    error.postValue(errorMessage);
                }
            });
        }
    }

// This method deletes an email.
// It calls the repository method to delete the email and updates the error LiveData if the operation
    public void deleteMail(String emailId) {
        mailRepository.deleteMail(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String id) {
            }
            @Override
            public void onFailure(String errorMessage) {
                error.postValue(errorMessage);
            }
        });
    }

    // Activate the API call
    public void fetchEmailById(String emailId) {
        mailRepository.getEmailById(emailId, new MailRepository.EmailDetailsCallback() {
            @Override
            public void onSuccess(Email email) {
                emailDetails.postValue(email);
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue(errorMessage);
            }
        });
    }
    // This method fetches all labels from the repository and updates the LiveData.
    // It uses a callback to handle success and failure cases.
    public void fetchAllLabels() { 
        mailRepository.getLabels(new MailRepository.LabelsCallback() {
            @Override
            public void onSuccess(List<Label> labels) {
                allLabels.postValue(labels);
            }

            @Override
            public void onFailure(String errorMsg) {
                error.postValue("Failed to load labels: " + errorMsg);
            }
        });
    }

// This method adds a label to an email.
// It calls the repository method to add the label and updates the error LiveData if the operation
    public void addMailToLabel(String emailId, String labelId) {
        mailRepository.addMailToLabel(emailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String id) {
                fetchEmailById(id); // After adding the label, refresh the email details to reflect the changes
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to add label: " + errorMessage);
            }
        });
    }

    // This method removes a label from an email.
    public void removeMailFromLabel(String emailId, String labelId) {
        mailRepository.removeMailFromLabel(emailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String id) {
                fetchEmailById(id); // After removing the label, refresh the email details to reflect the changes
            }

            @Override
            public void onFailure(String errorMessage) {
                error.postValue("Failed to remove label: " + errorMessage);
            }
        });
    }
}