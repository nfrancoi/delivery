package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Calendar;
import java.util.Objects;

@Entity
public class Employee implements BaseEntity{

    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long employeeId;
    public String name;
    public String notePrefix;

    public Calendar startDate;
    public Calendar endDate;
    public boolean isActive;
    public boolean isDefault;


    public Employee(Long employeeId, String name,  String notePrefix, boolean isActive ) {
        this.employeeId = employeeId;
        this.name = name;
        this.notePrefix = notePrefix;
        this.isActive = isActive;
        this.isDefault = false;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employee employee = (Employee) o;
        return isActive == employee.isActive &&
                isDefault == employee.isDefault &&
                employeeId.equals(employee.employeeId) &&
                Objects.equals(name, employee.name) &&
                Objects.equals(notePrefix, employee.notePrefix) &&
                Objects.equals(startDate, employee.startDate) &&
                Objects.equals(endDate, employee.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId, name, notePrefix, startDate, endDate, isActive, isDefault);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Long getId() {
        return employeeId;
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
