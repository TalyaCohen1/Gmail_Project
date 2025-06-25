package com.example.android_app.model.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.android_app.data.network.ApiClient;
import com.example.android_app.data.network.MailApiService;
import com.example.android_app.model.Email;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InboxViewModel extends ViewModel {

    private MutableLiveData<List<Email>> emails = new MutableLiveData<>();
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private MailApiService apiService = ApiClient.getMailApiService();

    public LiveData<List<Email>> getEmails() {
        return emails;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    // Function that the Activity will call to start the process
    public void fetchEmails() {
        isLoading.setValue(true);

        apiService.getInboxEmails().enqueue(new Callback<List<Email>>() {
            @Override
            public void onResponse(Call<List<Email>> call, Response<List<Email>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    emails.setValue(response.body());
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(Call<List<Email>> call, Throwable t) {
                // Handle web error
                emails.setValue(null); // Empty list
                isLoading.setValue(false);
            }
        });
    }
}
