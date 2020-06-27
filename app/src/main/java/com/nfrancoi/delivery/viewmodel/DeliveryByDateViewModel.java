package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Delivery;

import java.util.Calendar;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeliveryByDateViewModel extends AndroidViewModel {

    private Repository mRepository;


    private Calendar calendarDay;
    private LiveData<List<Delivery>> dayDeliveries;


    public DeliveryByDateViewModel(Application application, Calendar calendarDay) {
        super(application);
        this.calendarDay = calendarDay;
        mRepository = Repository.getInstance(application);
        dayDeliveries = mRepository.getDeliveriesByDay(calendarDay);
    }

    public LiveData<List<Delivery>> getDeliveries() {
        return dayDeliveries;
    }

    public Single<Long> insert(@NonNull Delivery deliveryP) {
        Single<Long> observable = mRepository.insert(deliveryP);
        observable = observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        return observable;
    }

}