package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.dao.DeliveryProductsJoinDao;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;
import com.nfrancoi.delivery.room.entities.PointOfDelivery;

import java.math.BigDecimal;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeliveryViewModel extends AndroidViewModel {

    private Repository mRepository;

    //in constructor only
    private Long deliveryId;

    private LiveData<Delivery> selectedDelivery;
    private LiveData<List<PointOfDelivery>> pointOfDeliveries;


    //
    //cache
    //
    private LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> selectedDepositDeliveryProductDetails;
    private LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> selectedTakeDeliveryProductDetails;
    private MediatorLiveData<BigDecimal> selectedDeliveryTotalAmount = new MediatorLiveData<>();


    public DeliveryViewModel(Application application, Long deliveryId) {
        super(application);
        this.deliveryId = deliveryId;
        mRepository = Repository.getInstance(application);

        selectedDelivery = mRepository.getDelivery(deliveryId);

        pointOfDeliveries = mRepository.getPointOfDeliveries();
        this.selectedDepositDeliveryProductDetails = mRepository.loadDepositDeliveryProductDetails(deliveryId);
        this.selectedTakeDeliveryProductDetails = mRepository.loadTakeDeliveryProductDetails(deliveryId);
        this.selectedDeliveryTotalAmount = new MediatorLiveData<>();
        this.selectedDeliveryTotalAmount.setValue(BigDecimal.ZERO);

        this.selectedDeliveryTotalAmount.addSource(selectedDepositDeliveryProductDetails, this::totalAmountObserver);

        this.selectedDeliveryTotalAmount.addSource(selectedTakeDeliveryProductDetails, this::totalAmountObserver);

    }

    private void totalAmountObserver(List<DeliveryProductsJoinDao.DeliveryProductDetail> deliveryProductDetails) {
        BigDecimal sumPriceDeposit = BigDecimal.ZERO;
        BigDecimal sumPriceTake = BigDecimal.ZERO;
        if (selectedDepositDeliveryProductDetails.getValue() != null) {
            sumPriceDeposit = selectedDepositDeliveryProductDetails.getValue().stream().map(value -> value.price.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (selectedTakeDeliveryProductDetails.getValue() != null) {
            sumPriceTake = selectedTakeDeliveryProductDetails.getValue().stream().map(value -> value.price.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        selectedDeliveryTotalAmount.setValue(sumPriceDeposit.add(sumPriceTake));
    }

    public LiveData<Delivery> getSelectedDelivery() {
        return selectedDelivery;
    }


    public LiveData<List<PointOfDelivery>> getPointOfDeliveries() {
        return pointOfDeliveries;
    }


    public void onSelectedPointOfDelivery(PointOfDelivery selectedPointOfDelivery) {
        Delivery selectedDelivery = this.selectedDelivery.getValue();
        selectedDelivery.pointOfDelivery = selectedPointOfDelivery;
        this.updateSelectedDelivery();
    }


    //
    // Deposit
    //
    public LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> getSelectedDepositDeliveryProductDetails() {
        return selectedDepositDeliveryProductDetails;
    }

    //
    // Take
    //
    public LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> getSelectedTakeDeliveryProductDetails() {
        return selectedTakeDeliveryProductDetails;
    }

    //
    // Total
    //

    public LiveData<BigDecimal> getSelectedDeliveryTotalAmount() {
        return selectedDeliveryTotalAmount;
    }


    //
    // Save
    //

    public Single<Long> insert(@NonNull Delivery deliveryP) {
        Single<Long> observable = mRepository.insert(deliveryP);
        observable = observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        return observable;
    }

    public void updateSelectedDelivery() {
        Delivery currentDelivery = selectedDelivery.getValue();
        mRepository.update(currentDelivery);
    }

    public void updateDelivery(Delivery delivery) {
        mRepository.update(delivery);
    }

    public void saveProductDetail(DeliveryProductsJoinDao.DeliveryProductDetail deliveryProductDetailToUpdate) {
        if (selectedDepositDeliveryProductDetails == null)
            throw new IllegalStateException("selectedDepositDeliveryProductDetails MUST not be null");

        //negative quantity for returns
        int quantity = deliveryProductDetailToUpdate.quantity * ("T".equals(deliveryProductDetailToUpdate.type) ? -1 : 1);
        DeliveryProductsJoin dp = new DeliveryProductsJoin(deliveryProductDetailToUpdate.deliveryId,
                deliveryProductDetailToUpdate.productId,
                deliveryProductDetailToUpdate.type,
                quantity,
                deliveryProductDetailToUpdate.price,
                deliveryProductDetailToUpdate.vat);

        if (dp.quantity == 0) {
            mRepository.deleteDeliveryProductJoin(dp.deliveryId, dp.productId, dp.type);
        } else {
            mRepository.insertReplace(dp);
        }
    }


}