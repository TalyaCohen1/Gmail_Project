package com.example.android_app.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.android_app.data.repository.UserRepository;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationViewModel extends AndroidViewModel {
    private final UserRepository repository;

    public MutableLiveData<String> status = new MutableLiveData<>();

    public RegistrationViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public void registerUser(String fullName, String email, String birthDate, String gender, String password, Uri imageUri) {
        repository.registerUser(fullName, email, birthDate, gender, password, imageUri)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            status.postValue("Registration successful");
                        } else {
                            status.postValue("Registration failed: " + response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        status.postValue("Error: " + t.getMessage());
                    }
                });
    }
}
