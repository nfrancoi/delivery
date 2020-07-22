package com.nfrancoi.delivery.room.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Objects;

@Entity
public class Employee {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long employeeId;
    public String name;
    public Calendar startDate;
    public Calendar endDate;
    public boolean isActive;
    public boolean isDefault;


    public Employee(String name) {
        this.name = name;
        this.isDefault = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return employeeId.equals(employee.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId);
    }

    @Override
    public String toString() {
        return name;
    }
}
