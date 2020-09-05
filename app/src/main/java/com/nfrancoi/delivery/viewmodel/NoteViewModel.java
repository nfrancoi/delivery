package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Company;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private LiveData<Company> company;
    private LiveData<List<DeliveryProductsJoin>> deliveryProductNoteDetails;

    private MediatorLiveData<Pair<Company, List<DeliveryProductsJoin>>> noteLiveData;

    private boolean companyCompleted = false;
    private boolean deliveryProductNoteDetailsCompleted = false;

    private BigDecimal totalVatExcl;
    private BigDecimal totalTaxes;
    private BigDecimal total;

    public NoteViewModel(Application application, Long deliveryId) {
        super(application);
        Repository mRepository = Repository.getInstance();

        company = mRepository.getFirstCompany();
        deliveryProductNoteDetails = mRepository.loadAllDeliveryProductDetails(deliveryId);

        noteLiveData = new MediatorLiveData<>();
        noteLiveData.addSource(company, new Observer<Company>() {
            @Override
            public void onChanged(Company company) {
                companyCompleted = true;
                isCompletedLoad();
            }
        });
        noteLiveData.addSource(deliveryProductNoteDetails, noteDetails -> {
            deliveryProductNoteDetailsCompleted = true;

            totalVatExcl = noteDetails.stream().map(noteDeliveryProductDetail -> noteDeliveryProductDetail.priceTotVatDiscounted).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);;

            totalTaxes = noteDetails.stream().map(noteDeliveryProductDetail ->
                    noteDeliveryProductDetail.priceTotVatDiscounted.multiply(noteDeliveryProductDetail.vat.divide(BigDecimal.valueOf(100l)))).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2,  RoundingMode.HALF_UP);;

            total = totalVatExcl.add(totalTaxes).setScale(2, RoundingMode.HALF_UP);;

            isCompletedLoad();

        });
    }

    private void isCompletedLoad() {
        if (companyCompleted && deliveryProductNoteDetailsCompleted) {
            Pair<Company, List<DeliveryProductsJoin>> pair
                    = new Pair<Company, List<DeliveryProductsJoin>>(company.getValue(), deliveryProductNoteDetails.getValue());

            noteLiveData.setValue(pair);

        }
    }

    public MediatorLiveData<Pair<Company, List<DeliveryProductsJoin>>> getNoteLiveData() {
        return noteLiveData;
    }

    public LiveData<Company> getCompany() {
        return company;
    }

    public LiveData<List<DeliveryProductsJoin>> getDeliveryProductNoteDetails() {
        return deliveryProductNoteDetails;
    }

    public BigDecimal getTotalVatExcl() {
        return totalVatExcl;
    }

    public BigDecimal getTotalTaxes() {
        return totalTaxes;
    }

    public BigDecimal getTotal() {
        return total;
    }
}