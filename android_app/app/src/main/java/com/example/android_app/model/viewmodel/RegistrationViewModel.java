package com.example.android_app.model.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.android_app.data.repository.UserRepository;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//class that handles the registration process- connect between view and repository
public class RegistrationViewModel extends AndroidViewModel {
    private final UserRepository repository; //interface for interacting with the Network layer

    public MutableLiveData<String> status = new MutableLiveData<>(); //live data to notify the view about the registration status

    public RegistrationViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    //call registerUser method from repository
    public void registerUser(String fullName, String email, String birthDate, String gender, String password, Uri imageUri) {
        repository.registerUser(fullName, email, birthDate, gender, password, imageUri)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            status.postValue("Registration successful");
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                JsonObject jsonError = new Gson().fromJson(errorBody, JsonObject.class);
                                String msg = null;

                                if (jsonError.has("message")) {
                                    msg = jsonError.get("message").getAsString();
                                } else if (jsonError.has("error")) {
                                    msg = jsonError.get("error").getAsString();
                                } else {
                                    msg = "Registration failed";
                                }
                                status.postValue(msg);
                            } catch (Exception e) {
                                status.postValue("Unexpected error occurred");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        status.postValue("Error: " + t.getMessage());
                    }
                });
    }
}
