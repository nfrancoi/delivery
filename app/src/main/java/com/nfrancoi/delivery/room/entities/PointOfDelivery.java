package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class PointOfDelivery implements BaseEntity{

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
        this.pointOfDeliveryId = pointOfDeliveryId;
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

    //
    // BaseEntity
    //
    @Override
    public Long getId() {
        return pointOfDeliveryId;
    }

    @Override
    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PointOfDelivery that = (PointOfDelivery) o;
        return isActive == that.isActive &&
                pointOfDeliveryId.equals(that.pointOfDeliveryId) &&
                Objects.equals(name, that.name) &&
                Objects.equals(address, that.address) &&
                Objects.equals(discountPercentage, that.discountPercentage) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pointOfDeliveryId, name, address, discountPercentage, isActive, email);
    }
}
