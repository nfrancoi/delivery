package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class NoteViewModelFactory extends ViewModelProvider.NewInstanceFactory {

    private Long deliveryId;
    private Application application;

    public NoteViewModelFactory(Application application, Long deliveryId) {

        this.deliveryId = deliveryId;
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new NoteViewModel(application,deliveryId);
    }
}