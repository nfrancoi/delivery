package com.nfrancoi.delivery.tools;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;
import com.nfrancoi.delivery.viewmodel.data.NoteData;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

public abstract class NoteCreator {


    private Delivery delivery;
    private NoteData noteData = new NoteData();
    private MediatorLiveData<NoteData> noteDataLiveData;


    public NoteCreator(Delivery delivery) {
        this.delivery = delivery;
        this.noteDataLiveData = new MediatorLiveData<>();

        Repository mRepository = Repository.getInstance();
        noteDataLiveData.addSource(mRepository.getFirstCompany(), company -> {
            noteData.company = company;
            isCompletedLoad();

        });

        //update calc data of the note

        noteDataLiveData.addSource(this.updateDeliveryProductsJoinsPrices(), deliveryProductsJoins -> {
            noteData.deliveryProductNoteDetails = deliveryProductsJoins;

            for(DeliveryProductsJoin deliveryProductNoteDetail : noteData.deliveryProductNoteDetails){
                if(deliveryProductNoteDetail.priceTotVatExclDiscounted.compareTo(BigDecimal.ZERO)>0){
                    noteData.totalDeposVatExcl=noteData.totalDeposVatExcl.add(deliveryProductNoteDetail.priceTotVatExclDiscounted);
                }else{
                    noteData.totalTakeVatExcl=noteData.totalTakeVatExcl.add(deliveryProductNoteDetail.priceTotVatExclDiscounted);
                }
            }
            noteData.totalTakeVatExcl = noteData.totalTakeVatExcl.setScale(2, RoundingMode.HALF_UP);
            noteData.totalDeposVatExcl = noteData.totalDeposVatExcl.setScale(2, RoundingMode.HALF_UP);

            //sort
            noteData.deliveryProductNoteDetails = deliveryProductsJoins.stream().sorted((o1, o2) -> o1.type.compareTo(o2.type)).collect(Collectors.toList());

            noteData.totalVatExcl =
                    deliveryProductsJoins.stream().map(
                            noteDeliveryProductDetail -> noteDeliveryProductDetail.priceTotVatExclDiscounted).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            ;

            noteData.totalTaxes = deliveryProductsJoins.stream().map(noteDeliveryProductDetail ->
                    noteDeliveryProductDetail.priceTotVatInclDiscounted.add(noteDeliveryProductDetail.priceTotVatExclDiscounted.negate())).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            ;
            noteData.total = deliveryProductsJoins.stream().map(noteDeliveryProductDetail ->
                    noteDeliveryProductDetail.priceTotVatInclDiscounted).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
            ;
            ;

            isCompletedLoad();
        });



    }

    abstract public LiveData<File> createNote();


    private void isCompletedLoad() {
        if (noteData.company != null && noteData.deliveryProductNoteDetails != null) {
            noteData.delivery = delivery;
            noteDataLiveData.postValue(noteData);
        }
    }


    private LiveData<List<DeliveryProductsJoin>> updateDeliveryProductsJoinsPrices() {
        Repository mRepository = Repository.getInstance();
        return mRepository.calculateDeliveryProductsJoinsPrices(delivery);
    }


    protected LiveData<NoteData> getNoteData() {
        return noteDataLiveData;
    }


}