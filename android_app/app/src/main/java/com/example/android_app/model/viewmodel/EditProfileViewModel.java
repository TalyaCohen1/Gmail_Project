package com.example.android_app.model.viewmodel;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.annotation.NonNull;
import android.app.Application;
import android.net.Uri;

import com.example.android_app.data.local.UserEntity;
import com.example.android_app.data.repository.UserRepository;
import com.example.android_app.utils.SharedPrefsManager;


public class EditProfileViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final MutableLiveData<String> successMsg = new MutableLiveData<>();
    private final MutableLiveData<String> errorMsg = new MutableLiveData<>();
    private final MutableLiveData<UserEntity> updatedUser = new MutableLiveData<>();


    public EditProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public void updateProfile(String name, Uri imageUri) {
        String userId = SharedPrefsManager.get(getApplication(), "userId");

        repository.updateUserProfile(userId, name, imageUri, successMsg, errorMsg);
        loadUpdatedUser();
    }
    public void loadUpdatedUser() {
        String userId = SharedPrefsManager.get(getApplication(), "userId");
        repository.getUserById(userId, new UserRepository.LocalCallback<UserEntity>() {
            @Override
            public void onResult(UserEntity result) {
                updatedUser.postValue(result);
            }
        });
    }


    public LiveData<String> getSuccessMessage() { return successMsg; }
    public LiveData<String> getErrorMessage() { return errorMsg; }
    public LiveData<UserEntity> getUpdatedUser() { return updatedUser; }

}
