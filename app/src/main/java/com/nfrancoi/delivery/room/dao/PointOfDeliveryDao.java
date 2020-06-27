package com.nfrancoi.delivery.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nfrancoi.delivery.room.entities.PointOfDelivery;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface PointOfDeliveryDao {

    // allowing the insertReplace of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(PointOfDelivery pointOfDelivery);

    @Query("SELECT * from PointOfDelivery")
    LiveData<List<PointOfDelivery>> getPointOfDeliveries();

    @Query("SELECT * from PointOfDelivery WHERE pointOfDeliveryId= :podId")
    LiveData<PointOfDelivery>getPointOfDelivery(Long podId);

    @Query("DELETE FROM PointOfDelivery")
    void deleteAll();
}