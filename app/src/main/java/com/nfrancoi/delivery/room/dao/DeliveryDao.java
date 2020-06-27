package com.nfrancoi.delivery.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.nfrancoi.delivery.room.entities.Delivery;

import java.util.Calendar;
import java.util.List;

@Dao
public abstract class DeliveryDao extends BaseDao<Delivery> {


    @Query("SELECT * from Delivery")
    abstract public LiveData<List<Delivery>> getDeliveries();

    @Query("SELECT * FROM Delivery d WHERE d.date BETWEEN :begin AND :end")
    abstract public LiveData<List<Delivery>> getDeliveriesByDates(Calendar begin, Calendar end);

    @Query("DELETE FROM Delivery")
    abstract public void deleteAll();

    @Query("SELECT * FROM Delivery d WHERE d.deliveryId = :deliveryId")
    public abstract LiveData<Delivery> getDelivery(Long deliveryId);
}