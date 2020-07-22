package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.util.Calendar;

@Entity
public class Product {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long productId;
    public String name;
    public BigDecimal priceHtUnit;
    public BigDecimal vat;


    public Calendar startDate;
    public Calendar endDate;
    public boolean isActive;

    public Product(String name, BigDecimal priceHtUnit, BigDecimal vat) {
        this.name = name;
        this.priceHtUnit = priceHtUnit;
        this.vat = vat;
        this.startDate = Calendar.getInstance();
        this.isActive = true;

    }
}
