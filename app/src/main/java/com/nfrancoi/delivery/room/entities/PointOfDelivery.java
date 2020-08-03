package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;

@Entity
public class PointOfDelivery {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long pointOfDeliveryId;
    public String name;
    public String address;
    public BigDecimal discountPercentage;


    public boolean isActive;

    public String email;

    @Ignore
    public PointOfDelivery(@NonNull String name,@NonNull String address,@NonNull BigDecimal discountPercentage,@NonNull String email ) {
        this.name = name;
        this.address = address;
        this.discountPercentage = discountPercentage;
        this.isActive = true;
        this.email = email;
    }

    public PointOfDelivery(@NonNull Long pointOfDeliveryId, @NonNull String name,@NonNull String address,@NonNull BigDecimal discountPercentage,@NonNull String email , boolean isActive) {
        this.name = name;
        this.address = address;
        this.discountPercentage = discountPercentage;
        this.isActive = isActive;
        this.email = email;
    }

    @Override
    public String toString() {
        return name;
    }
}
