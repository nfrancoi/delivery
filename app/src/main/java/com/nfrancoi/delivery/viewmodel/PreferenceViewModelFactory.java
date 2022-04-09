package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class PreferenceViewModelFactory implements ViewModelProvider.Factory {

    private Application application;

    public PreferenceViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new PreferencesViewModel(application);
    }
}