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

    public String productName;
    public int quantity;
    public BigDecimal priceUnitVatIncl;
    public BigDecimal vat;
    public BigDecimal discount;


    public BigDecimal priceUnitVatExcl;
    public BigDecimal priceTotVatDiscounted;



}

