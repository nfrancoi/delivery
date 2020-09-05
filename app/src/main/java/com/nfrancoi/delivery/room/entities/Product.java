package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.util.Calendar;

@Entity
public class Product implements BaseEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long productId;
    public String name;
    public String type;
    public BigDecimal priceUnitVatIncl;
    public BigDecimal vat;


    public Calendar startDate;
    public Calendar endDate;
    public boolean isActive;

    @Ignore
    public Product(String name, String type, BigDecimal priceUnitVatIncl, BigDecimal vat) {
        this.name = name;
        this.type = type;
        this.priceUnitVatIncl = priceUnitVatIncl;
        this.vat = vat;
        this.startDate = Calendar.getInstance();
        this.isActive = true;

    }

    public Product(Long productId, String name, String type, BigDecimal priceUnitVatIncl, BigDecimal vat, boolean isActive) {
        this.productId = productId;
        this.name = name;
        this.type = type;
        this.priceUnitVatIncl = priceUnitVatIncl;
        this.vat = vat;
        this.startDate = Calendar.getInstance();
        this.isActive = isActive;

    }

    @Override
    public Long getId() {
        return this.productId;
    }

    @Override
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }
}
