package com.nfrancoi.delivery.room.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Calendar;

@Entity
public class Employee {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long employeeId;
    public String name;
    public Calendar startDate;
    public Calendar endDate;
    public boolean isActive;


}
