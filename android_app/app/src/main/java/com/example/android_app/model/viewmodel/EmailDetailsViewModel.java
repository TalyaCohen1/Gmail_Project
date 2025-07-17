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

    // LiveData שה-Activity יאזין לו כדי לדעת אם הייתה שגיאה
    public LiveData<String> getError() {
        return error;
    }

    public LiveData<List<Label>> getAllLabels() {
        return allLabels;
    }

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

    public void deleteMail(String emailId) {
        mailRepository.deleteMail(emailId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String id) {
                // אין צורך לרענן את המייל אם חוזרים אחורה
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
    public void fetchAllLabels() { // המתודה שקוראת ל-Repository כדי להביא את הלייבלים
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

    public void addMailToLabel(String emailId, String labelId) {
        mailRepository.addMailToLabel(emailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String id) {
                // לאחר הוספת לייבל בהצלחה, רענן את פרטי המייל כדי שה-UI יתעדכן
                fetchEmailById(id);
            }

            @Override
            public void onFailure(String errorMessage) {
                // עדכן את LiveData השגיאות כדי שה-Activity יציג הודעה למשתמש
                error.postValue("Failed to add label: " + errorMessage);
            }
        });
    }

    // מתודה להסרת לייבל מהמייל
    public void removeMailFromLabel(String emailId, String labelId) {
        mailRepository.removeMailFromLabel(emailId, labelId, new MailRepository.MailActionCallback() {
            @Override
            public void onSuccess(String id) {
                // לאחר הסרת לייבל בהצלחה, רענן את פרטי המייל כדי שה-UI יתעדכן
                fetchEmailById(id);
            }

            @Override
            public void onFailure(String errorMessage) {
                // עדכן את LiveData השגיאות כדי שה-Activity יציג הודעה למשתמש
                error.postValue("Failed to remove label: " + errorMessage);
            }
        });
    }
}