package com.nfrancoi.delivery.room.entities;

import java.math.BigDecimal;
import java.util.Calendar;


public class DeliveryBillingSummary {
    public String pointOfDeliveryId;
    public String pointOfDeliveryName;
    public Calendar deliveryDate;
    public String noteId;
    public String noteRemoteURL;
    public BigDecimal vat;
    public BigDecimal priceTotVatExclDiscounted;
    public BigDecimal priceTotVatInclDiscounted;


}

