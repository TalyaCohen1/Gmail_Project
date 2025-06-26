package com.example.android_app.utils;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_app.model.viewmodel.EditProfileViewModel;

public class ViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;

    public ViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(EditProfileViewModel.class)) {
            return (T) new EditProfileViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
