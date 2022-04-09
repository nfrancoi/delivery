package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.Calendar;

public class DeliveryByDateViewModelFactory implements ViewModelProvider.Factory {

    private Calendar calendarDay;
    private Application application;

    public DeliveryByDateViewModelFactory(Application application, Calendar calendarDay) {

        this.calendarDay = calendarDay;
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new DeliveryByDateViewModel(application,calendarDay);
    }
}