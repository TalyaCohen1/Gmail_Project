package com.example.android_app.model.viewmodel;


import androidx.annotation.NonNull;
import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.android_app.model.LoginRequest;
import com.example.android_app.model.LoginResponse;
import com.example.android_app.data.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {
    private final UserRepository repository ;
    private final MutableLiveData<LoginResponse> loginResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }
    public LiveData<LoginResponse> getLoginResult() {
        return loginResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            errorMessage.setValue("Email and password are required");
            return;
        }

        LoginRequest request = new LoginRequest(email, password);
        repository.login(request, loginResult, errorMessage);
    }
}

