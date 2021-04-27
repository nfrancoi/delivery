package com.nfrancoi.delivery.viewmodel.data;

import com.nfrancoi.delivery.room.entities.Company;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;

import java.math.BigDecimal;
import java.util.List;

/*
Data required to generate the note
 */
public class NoteData {

    public Delivery delivery;
    public Company company;
    public List<DeliveryProductsJoin> deliveryProductNoteDetails;
    public BigDecimal totalVatExcl;
    public BigDecimal totalTaxes;
    public BigDecimal total;
}
