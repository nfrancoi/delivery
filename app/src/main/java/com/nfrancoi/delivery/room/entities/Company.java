package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity
public class Company implements BaseEntity {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long companyId;

    public String name;
    public String address;
    public String phoneNumber1;
    public String phoneNumber2;
    public String email;
    public String vatNumber;
    public String bankAccount;
    public boolean isActive;

    public Company(String name, String address, String phoneNumber1, String phoneNumber2, String email, String vatNumber, String bankAccount) {
        this.name = name;
        this.address = address;
        this.phoneNumber1 = phoneNumber1;
        this.phoneNumber2 = phoneNumber2;
        this.email = email;
        this.vatNumber = vatNumber;
        this.bankAccount = bankAccount;
        this.isActive = true;
    }

    public Company(Long id, String name, String address, String phoneNumber1, String phoneNumber2, String email, String vatNumber, String bankAccount, Boolean isActive) {
        this.companyId = id;
        this.name = name;
        this.address = address;
        this.phoneNumber1 = phoneNumber1;
        this.phoneNumber2 = phoneNumber2;
        this.email = email;
        this.vatNumber = vatNumber;
        this.bankAccount = bankAccount;
        this.isActive = isActive;
    }

    @Override
    public Long getId() {
        return companyId;
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
        return companyId + " " +name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Company company = (Company) o;
        return isActive == company.isActive &&
                companyId.equals(company.companyId) &&
                Objects.equals(name, company.name) &&
                Objects.equals(address, company.address) &&
                Objects.equals(phoneNumber1, company.phoneNumber1) &&
                Objects.equals(phoneNumber2, company.phoneNumber2) &&
                Objects.equals(email, company.email) &&
                Objects.equals(vatNumber, company.vatNumber) &&
                Objects.equals(bankAccount, company.bankAccount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyId, name, address, phoneNumber1, phoneNumber2, email, vatNumber, bankAccount, isActive);
    }
}

