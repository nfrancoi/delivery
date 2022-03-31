package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
public class Product implements BaseEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long productId;
    public String name;
    public String type;
    public BigDecimal priceUnitVatIncl;
    public BigDecimal vat;

    public boolean isActive;

    @Ignore
    public Product(String name, String type, BigDecimal priceUnitVatIncl, BigDecimal vat) {
        this.name = name;
        this.type = type;
        this.priceUnitVatIncl = priceUnitVatIncl;
        this.vat = vat;
        this.isActive = true;

    }

    public Product(Long productId, String name, String type, BigDecimal priceUnitVatIncl, BigDecimal vat, boolean isActive) {
        this.productId = productId;
        this.name = name;
        this.type = type;
        this.priceUnitVatIncl = priceUnitVatIncl;
        this.vat = vat;
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

    @Override
    public String toString() {
        return productId + " " +name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return isActive == product.isActive &&
                productId.equals(product.productId) &&
                Objects.equals(name, product.name) &&
                Objects.equals(type, product.type) &&
                Objects.equals(priceUnitVatIncl, product.priceUnitVatIncl) &&
                Objects.equals(vat, product.vat);

    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, name, type, priceUnitVatIncl, vat,isActive);
    }
}
