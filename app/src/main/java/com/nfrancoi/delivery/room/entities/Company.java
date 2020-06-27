package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Company {
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

    public Company(String name, String address, String phoneNumber1, String phoneNumber2, String email, String vatNumber, String bankAccount) {
        this.name = name;
        this.address = address;
        this.phoneNumber1 = phoneNumber1;
        this.phoneNumber2 = phoneNumber2;
        this.email = email;
        this.vatNumber = vatNumber;
        this.bankAccount = bankAccount;
    }
}

