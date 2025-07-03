package com.example.android_app.model.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android_app.data.network.MailService;
import com.example.android_app.data.repository.MailRepository;
import com.example.android_app.model.Email;
import com.example.android_app.model.EmailRequest; // וודא שזה מיובא
import com.example.android_app.utils.SendCallback;
import com.example.android_app.utils.SharedPrefsManager;

public class CreateMailViewModel extends AndroidViewModel {

    private static final String TAG = "CreateMailViewModel"; // הוסף לוג לניפוי באגים

    private final MailRepository repository;
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> emailSent = new MutableLiveData<>();
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false); // מצב טעינה

    // המשתנה הראשי לניהול אובייקט הטיוטה הנוכחי (או המייל החדש)
    private final MutableLiveData<Email> _currentDraft = new MutableLiveData<>();
    public LiveData<Email> getCurrentDraft() {
        return _currentDraft;
    }

    // מציין הצלחה כללית של פעולה (שמירה, שליחה)
    private final MutableLiveData<Boolean> _actionSuccess = new MutableLiveData<>();
    public LiveData<Boolean> getActionSuccess() {
        return _actionSuccess;
    }

    public LiveData<Boolean> getIsLoading() {
        return _isLoading;
    }

    public CreateMailViewModel(@NonNull Application application) {
        super(application);
        repository = new MailRepository(application);
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getEmailSent() {
        return emailSent;
    }

    // 1. מתודה לטעינת טיוטה קיימת או יצירת טיוטה חדשה
    public void loadOrCreateDraft(String existingMailId, String defaultTo, String defaultSubject, String defaultBody) {
        _isLoading.postValue(true);
        String token = SharedPrefsManager.get(getApplication(), "token");
        if (token == null || token.isEmpty()) {
            errorMessage.setValue("Authentication token is missing. Cannot load or create draft.");
            _isLoading.postValue(false);
            return;
        }

        if (existingMailId != null && !existingMailId.isEmpty()) {
            // טען טיוטה קיימת
            repository.getEmailById(existingMailId, new MailRepository.EmailDetailsCallback() {
                @Override
                public void onSuccess(Email email) {
                    _currentDraft.setValue(email); // הגדר את הטיוטה שנטענה
                    errorMessage.setValue(null);
                    _isLoading.postValue(false);
                }

                @Override
                public void onFailure(String error) {
                    errorMessage.setValue("Failed to load draft: " + error);
                    _isLoading.postValue(false);
                    // אם טיוטה קיימת לא נמצאה, צור טיוטה חדשה עם ערכי ברירת המחדל
                    // למקרה חירום, כדי לא להשאיר את המשתמש תקוע
                    // אך במצב רגיל, loadOrCreateDraft לא אמור להיכשל כאן
                    EmailRequest newDraftRequest = new EmailRequest(defaultTo, defaultSubject, defaultBody);
                    repository.createDraft(newDraftRequest, new MailService.DraftMailCallback() {
                        @Override
                        public void onSuccess(Email email) {
                            _currentDraft.setValue(email);
                        }

                        @Override
                        public void onFailure(String error) {
                            errorMessage.setValue("Also failed to create new draft: " + error);
                        }
                    });
                }
            });
        } else {
            // צור טיוטה חדשה ב-Backend עם התוכן הראשוני
            // השתמש ב-defaultTo, defaultSubject, defaultBody מהפרמטרים
            EmailRequest newDraftRequest = new EmailRequest(defaultTo, defaultSubject, defaultBody);

            repository.createDraft(newDraftRequest, new MailService.DraftMailCallback() {
                @Override
                public void onSuccess(Email email) {
                    _currentDraft.setValue(email); // הגדר את הטיוטה שנוצרה (כעת עם תוכן התחלתי!)
                    errorMessage.setValue(null);
                    _isLoading.postValue(false);
                }

                @Override
                public void onFailure(String error) {
                    errorMessage.setValue("Failed to create new draft: " + error);
                    _isLoading.postValue(false);
                }
            });
        }
    }

    // 2. מתודה לשמירת/עדכון טיוטה
    public void saveDraft(String to, String subject, String body) {
        Email current = _currentDraft.getValue();
        if (current == null || current.getId() == null) {
            Log.w(TAG, "No current draft ID to save. Skipping save.");
            // זו לא אמורה לקרות אם loadOrCreateDraft נקרא בהתחלה
            return;
        }

        String token = SharedPrefsManager.get(getApplication(), "token");
        if (token == null || token.isEmpty()) {
            errorMessage.setValue("Authentication token is missing. Cannot save draft.");
            return;
        }

        // עדכן את הטיוטה הקיימת
        repository.updateDraft(current.getId(), to, subject, body, token, new MailService.DraftMailCallback() {
            @Override
            public void onSuccess(Email updatedEmail) {
                _currentDraft.setValue(updatedEmail); // עדכן את הטיוטה ב-ViewModel
                _actionSuccess.postValue(true);
                errorMessage.setValue(null);
                Log.d(TAG, "Draft saved successfully.");
            }

            @Override
            public void onFailure(String error) {
                errorMessage.setValue("Failed to save draft: " + error);
                _actionSuccess.postValue(false);
                Log.e(TAG, "Failed to save draft: " + error);
            }
        });
    }

    public void sendEmail(String mailId, String to, String subject, String body) {
        _isLoading.postValue(true);
        errorMessage.setValue(null);
        _actionSuccess.postValue(false);

        if (TextUtils.isEmpty(to)) {
            errorMessage.setValue("Recipient is required");
            _isLoading.postValue(false);
            _actionSuccess.postValue(false);
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(to).matches()) {
            errorMessage.setValue("Invalid email address");
            _isLoading.postValue(false);
            _actionSuccess.postValue(false);
            return;
        }

        String token = SharedPrefsManager.get(getApplication(), "token");
        if (token == null || token.isEmpty()) {
            errorMessage.setValue("Authentication token not found. Please log in again.");
            _isLoading.postValue(false);
            _actionSuccess.postValue(false);
            return;
        }

        // שלח את המייל
        repository.sendEmail(mailId, to, subject, body, token, new SendCallback() { // <--- וודא שאתה מעביר את mailId ל-repository
            @Override
            public void onSuccess() {
                emailSent.setValue(true);
                errorMessage.setValue(null);
                _isLoading.postValue(false);
                _actionSuccess.postValue(true);
                Log.d(TAG, "Email sent successfully.");

                // *** כעת זה יפעל כראוי עם ה-mailId ***
                if (mailId != null && !mailId.isEmpty()) {
                    repository.deleteDraft(mailId, new MailRepository.ActionCallback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "Draft with ID " + mailId + " successfully deleted from local DB.");
                            _currentDraft.postValue(null);
                        }
                        @Override
                        public void onFailure(String error) {
                            Log.e(TAG, "Failed to delete draft with ID " + mailId + " from local DB: " + error);
                        }
                    });
                } else {
                    _currentDraft.postValue(null);
                }
            }

            @Override
            public void onFailure(String error) {
                emailSent.setValue(false);
                errorMessage.setValue(error);
                _isLoading.postValue(false);
                _actionSuccess.postValue(false);
                Log.e(TAG, "Failed to send email: " + error);
            }
        });
    }

}