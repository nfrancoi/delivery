package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;

@Entity(
        foreignKeys = {
                @ForeignKey(entity = Delivery.class, parentColumns = "deliveryId", childColumns = "deliveryId"),
                @ForeignKey(entity = Product.class, parentColumns = "productId", childColumns = "productId")}
)

public class DeliveryProductsJoin {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long deliveryProductsId;

    @NonNull
    public Long deliveryId;

    public Long productId;
    @NonNull
    public String type; //D:Deposit, T:Take, S:Sell

    public String productName;
    public int quantity;
    public BigDecimal priceUnitVatIncl;
    public BigDecimal vat;

    public BigDecimal vatApplicable;
    public BigDecimal priceUnitVatExcl;
    public BigDecimal discount;
    public BigDecimal priceTotVatExclDiscounted;
    public BigDecimal priceTotVatInclDiscounted;



}

