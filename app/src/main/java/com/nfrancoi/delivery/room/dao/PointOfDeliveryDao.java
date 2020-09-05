package com.nfrancoi.delivery.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.nfrancoi.delivery.room.entities.PointOfDelivery;

import java.util.List;

@Dao
public abstract class PointOfDeliveryDao extends BaseDao<PointOfDelivery>  {

    // allowing the insertReplace of the same word multiple times by passing a
    // conflict resolution strategy

    @Query("SELECT * from PointOfDelivery order by pointOfDeliveryId asc")
    public abstract List<PointOfDelivery> getAllSync();

    @Query("SELECT * from PointOfDelivery where isActive = 1")
    public abstract LiveData<List<PointOfDelivery>> getPointOfDeliveriesActive();

    @Query("SELECT * from PointOfDelivery WHERE pointOfDeliveryId= :podId")
    public abstract LiveData<PointOfDelivery>getPointOfDelivery(Long podId);

    @Query("DELETE FROM PointOfDelivery")
    public abstract void deleteAll();
}