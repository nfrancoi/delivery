package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import java.math.BigDecimal;

@Entity(primaryKeys = {"deliveryId", "productId", "type"},
        foreignKeys = {
                @ForeignKey(entity = Delivery.class, parentColumns = "deliveryId", childColumns = "deliveryId"),
                @ForeignKey(entity = Product.class, parentColumns = "productId", childColumns = "productId")}
)

public class DeliveryProductsJoin {

    @NonNull
    public Long deliveryId;
    @NonNull
    public Long productId;
    @NonNull
    public String type; //D:Deposit, T:Take

    public int quantity;
    public BigDecimal priceHtUnit;
    public BigDecimal priceHtTot;
    public BigDecimal vat;
    public BigDecimal discount;




    public DeliveryProductsJoin(@NonNull Long deliveryId, @NonNull Long productId , @NonNull String type, int quantity, BigDecimal priceHtUnit, BigDecimal priceHtTot, BigDecimal vat, BigDecimal discount) {
        this.deliveryId = deliveryId;
        this.productId = productId;
        this.quantity = quantity;
        this.priceHtUnit = priceHtUnit;
        this.priceHtTot = priceHtTot;

        this.vat = vat;
        this.discount = discount;
        this.type = type;
    }
}

