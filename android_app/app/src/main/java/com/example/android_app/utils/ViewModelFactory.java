package com.example.android_app.utils;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_app.model.viewmodel.EditProfileViewModel;
import com.example.android_app.model.viewmodel.CreateMailViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public ViewModelFactory(Application application) {
        super();
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EditProfileViewModel.class)) {
            return (T) new EditProfileViewModel(application);
        }
        if (modelClass.isAssignableFrom(CreateMailViewModel.class)) {
            return (T) new CreateMailViewModel(application);
        }

        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
