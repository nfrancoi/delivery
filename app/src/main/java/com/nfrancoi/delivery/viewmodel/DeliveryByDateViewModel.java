package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Delivery;

import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

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
        mRepository = Repository.getInstance();
        dayDeliveries = Transformations.switchMap(mRepository.getDeliveriesByDay(calendarDay), deliveries -> {
            List<Delivery> sortedDescDeliveries = deliveries.stream().sorted((d1, d2) -> d1.startDate.before(d2.startDate) ? 1 : -1).collect(Collectors.toList());
            return new MutableLiveData<>(sortedDescDeliveries);
        });



    }

    public LiveData<List<Delivery>> getDeliveries() {
        return dayDeliveries;
    }

    public Single<Long> insert(@NonNull Delivery deliveryP) {
        Single<Long> observable = mRepository.insertReplace(deliveryP);
        observable = observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        return observable;
    }

}