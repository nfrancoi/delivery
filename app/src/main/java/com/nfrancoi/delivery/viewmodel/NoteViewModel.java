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

import java.util.List;

public class NoteViewModel extends AndroidViewModel {

    private LiveData<Company> company;
    private LiveData<List<DeliveryProductsJoinDao.NoteDeliveryProductDetail>> deliveryProductNoteDetails;

    private MediatorLiveData<Pair<Company, List<DeliveryProductsJoinDao.NoteDeliveryProductDetail>>> noteLivedata;

    private boolean companyCompleted = false;
    private boolean deliveryProductNoteDetailsCompleted = false;

    public NoteViewModel(Application application, Long deliveryId) {
        super(application);
        Repository mRepository = Repository.getInstance(application);

        company = mRepository.getFirstCompany();
        deliveryProductNoteDetails = mRepository.loadTakeDeliveryProducNotetDetails(deliveryId);

        noteLivedata = new MediatorLiveData<>();
        noteLivedata.addSource(company, new Observer<Company>() {
            @Override
            public void onChanged(Company company) {
                companyCompleted = true;
                isCompletedLoad();
            }
        });
        noteLivedata.addSource(deliveryProductNoteDetails, noteDetails -> {
            deliveryProductNoteDetailsCompleted = true;
            isCompletedLoad();

        });
    }

    private void isCompletedLoad() {
        if (companyCompleted && deliveryProductNoteDetailsCompleted) {
            Pair<Company, List<DeliveryProductsJoinDao.NoteDeliveryProductDetail>> pair
                    = new Pair<Company, List<DeliveryProductsJoinDao.NoteDeliveryProductDetail>>(company.getValue(), deliveryProductNoteDetails.getValue());

            noteLivedata.setValue(pair);

        }
    }

    public MediatorLiveData<Pair<Company, List<DeliveryProductsJoinDao.NoteDeliveryProductDetail>>> getNoteLiveData() {
        return noteLivedata;
    }
}