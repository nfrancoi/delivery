package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.dao.DeliveryProductsJoinDao;
import com.nfrancoi.delivery.room.entities.Company;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private LiveData<Company> company;
    private LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> deliveryProductNoteDetails;

    private MediatorLiveData<Pair<Company, List<DeliveryProductsJoinDao.DeliveryProductDetail>>> noteLiveData;

    private boolean companyCompleted = false;
    private boolean deliveryProductNoteDetailsCompleted = false;

    private BigDecimal totalHt;
    private BigDecimal totalTaxes;
    private BigDecimal total;

    public NoteViewModel(Application application, Long deliveryId) {
        super(application);
        Repository mRepository = Repository.getInstance(application);

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

            totalHt = noteDetails.stream().map(noteDeliveryProductDetail -> noteDeliveryProductDetail.priceHtTot).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.CEILING);;
            totalTaxes = noteDetails.stream().map(noteDeliveryProductDetail ->
                    noteDeliveryProductDetail.priceHtTot.multiply(
                            noteDeliveryProductDetail.vat.divide(BigDecimal.valueOf(100)))).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.CEILING);;

            total = totalHt.add(totalTaxes).setScale(2, RoundingMode.CEILING);;

            isCompletedLoad();

        });
    }

    private void isCompletedLoad() {
        if (companyCompleted && deliveryProductNoteDetailsCompleted) {
            Pair<Company, List<DeliveryProductsJoinDao.DeliveryProductDetail>> pair
                    = new Pair<Company, List<DeliveryProductsJoinDao.DeliveryProductDetail>>(company.getValue(), deliveryProductNoteDetails.getValue());

            noteLiveData.setValue(pair);

        }
    }

    public MediatorLiveData<Pair<Company, List<DeliveryProductsJoinDao.DeliveryProductDetail>>> getNoteLiveData() {
        return noteLiveData;
    }

    public LiveData<Company> getCompany() {
        return company;
    }

    public LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> getDeliveryProductNoteDetails() {
        return deliveryProductNoteDetails;
    }

    public BigDecimal getTotalHt() {
        return totalHt;
    }

    public BigDecimal getTotalTaxes() {
        return totalTaxes;
    }

    public BigDecimal getTotal() {
        return total;
    }
}