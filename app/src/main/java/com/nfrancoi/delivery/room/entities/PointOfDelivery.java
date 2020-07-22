package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.util.Calendar;

@Entity
public class PointOfDelivery {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long pointOfDeliveryId;
    public String name;
    public String address;
    public BigDecimal discountPercentage;


    public Calendar startDate;
    public Calendar endDate;
    public boolean isActive;

    public String email;
    

    public PointOfDelivery(@NonNull String name,@NonNull String address,@NonNull BigDecimal discountPercentage,@NonNull String email ) {
        this.name = name;
        this.address = address;
        this.discountPercentage = discountPercentage;
        this.startDate = Calendar.getInstance();
        this.isActive = true;
        this.email = email;
    }

    @Override
    public String toString() {
        return name;
    }
}
