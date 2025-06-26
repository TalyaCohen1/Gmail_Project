package com.example.android_app.model.viewmodel;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import android.app.Application;
import android.net.Uri;
import com.example.android_app.data.repository.UserRepository;
import com.example.android_app.utils.SharedPrefsManager;


public class EditProfileViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final MutableLiveData<String> successMsg = new MutableLiveData<>();
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();

    public EditProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public void updateProfile(String name, Uri imageUri) {
        String userId = SharedPrefsManager.get(getApplication(), "userId");

        repository.updateUserProfile(userId, name, imageUri, successMsg, errorMsg);
    }


    public LiveData<String> getSuccessMessage() { return successMsg; }
    public LiveData<String> getErrorMessage() { return errorMsg; }
}
