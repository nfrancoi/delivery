package com.nfrancoi.delivery.room.entities;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.Calendar;

@Entity(foreignKeys = {
        @ForeignKey(entity = PointOfDelivery.class, parentColumns = "pointOfDeliveryId", childColumns = "pod_pointOfDeliveryId"),
        @ForeignKey(entity = Employee.class, parentColumns = "employeeId", childColumns = "employee_employeeId")}
)

public class Delivery {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Long deliveryId;
    public Calendar startDate;
    public Calendar sentDate;

    public String receiverName;
    public String commentDelivery;
    public String commentReceiver;

    public byte[] signatureBytes;

    public String noteId;
    public String noteURI;

    //Foreign keys
    @Embedded(prefix = "employee_")
    public Employee employee;

    @Embedded(prefix = "pod_")
    public PointOfDelivery pointOfDelivery;

    public Delivery(){
        startDate = Calendar.getInstance();
    }


}

