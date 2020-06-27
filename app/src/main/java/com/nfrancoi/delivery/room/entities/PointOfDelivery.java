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

    public PointOfDelivery(String name, String address,BigDecimal discountPercentage ) {
        this.name = name;
        this.address = address;
        this.discountPercentage = discountPercentage;
        this.startDate = Calendar.getInstance();
        this.isActive = true;
    }

    @Override
    public String toString() {
        return name;
    }
}
