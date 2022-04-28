package com.nfrancoi.delivery.room.dao;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;

import java.util.List;

@Dao
public interface DeliveryProductsJoinDao {

    @Query("SELECT * from DeliveryProductsJoin WHERE deliveryProductsId= :deliveryProductsJoinId")
    LiveData<DeliveryProductsJoin> getDeliveryProductsJoin(Long deliveryProductsJoinId);

    // allowing the insertReplace of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReplace(DeliveryProductsJoin deliveryProductsJoin);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(List<DeliveryProductsJoin> deliveryProductsJoins);

    @Query("SELECT * from DeliveryProductsJoin")
    LiveData<List<DeliveryProductsJoin>> getDeliveryProductsJoin();

    @Query("DELETE FROM DeliveryProductsJoin")
    void deleteAll();

    @Delete
    void delete(DeliveryProductsJoin deliveryProductsJoin);


    @Query("SELECT dp.deliveryProductsId, d.deliveryId AS deliveryId, p.productId AS productId, :type AS type, p.name AS productName," +
            " p.priceUnitVatIncl AS priceUnitVatIncl, dp.quantity AS quantity, p.vat AS vat, dp.discount AS discount " +
            "FROM  Product p, Delivery d " +
            "LEFT JOIN DeliveryProductsJoin dp ON d.deliveryId = dp.deliveryId AND p.productId = dp.productId AND (dp.type IS NULL OR dp.type = :type) " +
            "WHERE d.deliveryId = :deliveryId " +
            "AND p.isActive = 1")
    LiveData<List<DeliveryProductsJoin>> loadDeliveryProducts(@NonNull Long deliveryId, String type);

    @Query("SELECT dp.deliveryProductsId, dp.deliveryId AS deliveryId, dp.type AS type, dp.productName AS productName," +
            " dp.priceUnitVatIncl AS priceUnitVatIncl, dp.quantity AS quantity, dp.vat AS vat, dp.discount AS discount " +
            "FROM  DeliveryProductsJoin dp " +
            "WHERE dp.deliveryId = :deliveryId AND dp.type = 'S'")
    LiveData<List<DeliveryProductsJoin>> loadSellDeliveryProducts(@NonNull Long deliveryId);
    
    @Query("SELECT dp.deliveryProductsId, dp.deliveryId AS deliveryId, p.productId AS productId, dp.type AS type, dp.productName  AS productName, dp.priceUnitVatIncl AS priceUnitVatIncl, dp.priceUnitVatExcl AS priceUnitVatExcl,dp.priceTotVatExclDiscounted AS priceTotVatExclDiscounted, dp.priceTotVatInclDiscounted AS priceTotVatInclDiscounted, dp.quantity AS quantity, dp.vat AS vat, dp.vatApplicable AS vatApplicable, dp.discount AS discount " +
            "FROM DeliveryProductsJoin dp " +
            "LEFT JOIN Product p ON p.productId = dp.productId " +
            "WHERE dp.deliveryId = :deliveryId")
    List<DeliveryProductsJoin> loadNoteDeliveryProductDetailSync(@NonNull Long deliveryId);


}