package com.nfrancoi.delivery.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.DeliveryBillingSummary;

import java.util.Calendar;
import java.util.List;

@Dao
public abstract class DeliveryDao extends BaseDao<Delivery> {


    @Query("SELECT * from Delivery")
    abstract public LiveData<List<Delivery>> getDeliveries();

    @Query("SELECT * FROM Delivery d WHERE d.startDate BETWEEN :begin AND :end")
    abstract public LiveData<List<Delivery>> getDeliveriesByDates(Calendar begin, Calendar end);

    @Query("DELETE FROM Delivery")
    abstract public void deleteAll();

    @Query("SELECT * FROM Delivery d WHERE d.deliveryId = :deliveryId")
    public abstract LiveData<Delivery> getDelivery(Long deliveryId);

    @Query("SELECT * FROM Delivery d WHERE d.deliveryId = :deliveryId")
    public abstract Delivery getDeliverySync(Long deliveryId);

    @Query("SELECT * FROM Delivery d WHERE d.noteURI LIKE'%' || :noteFileName || '%'")
    public abstract Delivery getDeliveryFromNoteFileNameSync(String noteFileName);

    @Query("UPDATE Delivery SET isNoteSaved = 1 WHERE deliveryId = :deliveryId")
    public abstract int updateDeliveryNoteSent(Long deliveryId);

    @Query("SELECT pod.pointOfDeliveryId AS pointOfDeliveryId" +
            ", pod.name AS pointOfDeliveryName" +
            ", d.startDate AS deliveryDate" +
            ", d.noteId AS noteId" +
            ", dpj.vat AS vat" +
            ", sum(dpj.priceTotVatExclDiscounted) AS priceTotVatExclDiscounted" +
            ", sum(dpj.priceTotVatInclDiscounted) AS priceTotVatInclDiscounted " +
            "FROM Delivery d  " +
            "LEFT JOIN DeliveryProductsJoin dpj ON d.deliveryId = dpj.deliveryId " +
            "LEFT JOIN PointOfDelivery pod ON d.pod_pointOfDeliveryId = pod.pointOfDeliveryId " +
            "WHERE d.deliveryId = :deliveryId " +
            "GROUP BY pod.pointOfDeliveryId, pod.name, d.startDate,d.noteId, dpj.vat")
    public abstract List<DeliveryBillingSummary> loadDeliveryBillingSummarySync(Long deliveryId);
}