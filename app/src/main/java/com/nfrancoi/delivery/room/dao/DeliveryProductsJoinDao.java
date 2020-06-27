package com.nfrancoi.delivery.room.dao;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;

import java.math.BigDecimal;
import java.util.List;

@Dao
public interface DeliveryProductsJoinDao {

    // allowing the insertReplace of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReplace(DeliveryProductsJoin deliveryProductsJoin);

    @Query("SELECT * from DeliveryProductsJoin")
    LiveData<List<DeliveryProductsJoin>> getDeliveryProductsJoin();

    @Query("DELETE FROM DeliveryProductsJoin")
    void deleteAll();

    @Query("DELETE FROM DeliveryProductsJoin WHERE deliveryId = :deliveryId AND productId =:productId AND type = :type")
    void delete(Long deliveryId, Long productId, String type);





    class DeliveryProductDetail{
        public Long deliveryId;
        public Long productId;
        public String type;
        public String productName;
        public BigDecimal price;
        public int quantity;
        public BigDecimal vat;
    }
    @Query("SELECT d.deliveryId AS deliveryId, p.productId AS productId, :type AS type, p.name AS productName, p.price AS price, dp.quantity AS quantity "  +
            "FROM  Product p, Delivery d " +
            "LEFT JOIN DeliveryProductsJoin dp ON d.deliveryId = dp.deliveryId AND p.productId = dp.productId AND (dp.type IS NULL OR dp.type = :type) "+
            "WHERE d.deliveryId = :deliveryId ")
    LiveData<List<DeliveryProductDetail>> loadDeliveryProductDetails(@NonNull Long deliveryId, String type);


    class NoteDeliveryProductDetail {
        public Long deliveryId;
        public Long productId;
        public String type;
        public String productName;
        public BigDecimal price;
        public int quantity;
        public BigDecimal vat;
    }

    @Query("SELECT dp.deliveryId AS deliveryId, p.productId AS productId, dp.type AS type, p.name AS productName, dp.price AS price, dp.quantity AS quantity, dp.vat AS vat "  +
            "FROM DeliveryProductsJoin dp " +
            "JOIN Product p ON p.productId = dp.productId " +
            "WHERE dp.deliveryId = :deliveryId")
    LiveData<List<NoteDeliveryProductDetail>> loadNoteDeliveryProductDetail(@NonNull Long deliveryId);

}