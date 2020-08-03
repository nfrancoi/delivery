package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.dao.DeliveryProductsJoinDao;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;
import com.nfrancoi.delivery.room.entities.Employee;
import com.nfrancoi.delivery.room.entities.PointOfDelivery;
import com.nfrancoi.delivery.tools.StringTools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class DeliveryViewModel extends AndroidViewModel {

    private Repository mRepository;

    //in constructor only
    private Long deliveryId;

    private LiveData<Delivery> selectedDelivery;

    private LiveData<List<PointOfDelivery>> pointOfDeliveries;
    private LiveData<List<Employee>> employees;
    private LiveData<Employee> employeeByDefault;


    //
    //cache
    //
    private LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> selectedDepositDeliveryProductDetails;
    private MutableLiveData<String> filterDepositDeliveryProductDetails;
    private LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> filteredDepositDeliveryProductDetails;

    private LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> selectedTakeDeliveryProductDetails;
    private MediatorLiveData<BigDecimal> selectedDeliveryTotalAmount = new MediatorLiveData<>();


    public DeliveryViewModel(Application application, Long deliveryId) {
        super(application);
        this.deliveryId = deliveryId;
        mRepository = Repository.getInstance();

        selectedDelivery = mRepository.getDelivery(deliveryId);
        employees = mRepository.getEmployees();
        employeeByDefault = mRepository.getEmployeeByDefault();

        pointOfDeliveries = mRepository.getPointOfDeliveries();

        //
        // Deposit
        //
        this.selectedDepositDeliveryProductDetails = mRepository.loadDepositDeliveryProductDetails(deliveryId);
        this.filterDepositDeliveryProductDetails = new MediatorLiveData<>();
        this.filterDepositDeliveryProductDetails.setValue("");
        this.filteredDepositDeliveryProductDetails = Transformations.switchMap(filterDepositDeliveryProductDetails, filter -> {
            if (filter == null || filter.equals("") || selectedDepositDeliveryProductDetails.getValue() == null) {
                return selectedDepositDeliveryProductDetails;
            } else {
                MutableLiveData filteredLiveData = new MutableLiveData();
                List<DeliveryProductsJoinDao.DeliveryProductDetail> listFiltered =
                        selectedDepositDeliveryProductDetails.getValue().stream()
                                .filter(deliveryProductDetail -> deliveryProductDetail.productName.toLowerCase().contains(filter.toLowerCase())
                                                                || StringTools.PriceFormat.format(deliveryProductDetail.priceHtUnit).contains(filter.toLowerCase()))
                                .collect(Collectors.toList());
                filteredLiveData.setValue(listFiltered);

                return filteredLiveData;
            }
        });

        this.selectedTakeDeliveryProductDetails = mRepository.loadTakeDeliveryProductDetails(deliveryId);
        this.selectedDeliveryTotalAmount = new MediatorLiveData<>();
        this.selectedDeliveryTotalAmount.setValue(BigDecimal.ZERO);

        this.selectedDeliveryTotalAmount.addSource(selectedDepositDeliveryProductDetails, this::totalAmountObserver);

        this.selectedDeliveryTotalAmount.addSource(selectedTakeDeliveryProductDetails, this::totalAmountObserver);

    }

    //
    // Delivery
    //
    public LiveData<Delivery> getSelectedDelivery() {
        return selectedDelivery;
    }


    //
    // Deposit productDetails
    //
    public void filterDepositDeliveryProductDetails(String filter) {
        this.filterDepositDeliveryProductDetails.setValue(filter);
    }

    public LiveData<String> getFilterDepositDeliveryProductDetails() {
        return filterDepositDeliveryProductDetails;
    }

    public LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> getSelectedDepositDeliveryProductDetails() {
        return selectedDepositDeliveryProductDetails;
    }


    private void totalAmountObserver(List<DeliveryProductsJoinDao.DeliveryProductDetail> deliveryProductDetails) {
        BigDecimal sumPriceDeposit = BigDecimal.ZERO;
        BigDecimal sumPriceTake = BigDecimal.ZERO;
        if (selectedDepositDeliveryProductDetails.getValue() != null) {
            sumPriceDeposit = selectedDepositDeliveryProductDetails.getValue().stream().map(value -> value.priceHtUnit.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (selectedTakeDeliveryProductDetails.getValue() != null) {
            sumPriceTake = selectedTakeDeliveryProductDetails.getValue().stream().map(value -> value.priceHtUnit.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        selectedDeliveryTotalAmount.setValue(sumPriceDeposit.add(sumPriceTake));
    }

    public LiveData<List<DeliveryProductsJoinDao.DeliveryProductDetail>> getFilteredDepositDeliveryProductDetails() {
        return filteredDepositDeliveryProductDetails;
    }


    //
    // Status
    //
    public boolean isDeliverySigned(Delivery delivery) {
        if (delivery.pointOfDelivery != null && delivery.noteURI != null && delivery.signatureBytes != null)
            return true;
        else
            return false;
    }


    public boolean isDeliveryReadyToSign(Delivery delivery) {
        if (delivery.pointOfDelivery != null && delivery.noteURI != null)
            return true;
        else
            return false;
    }

    public boolean isDeliveryReadyToGenerateDocuments(Delivery delivery) {
        if (delivery.pointOfDelivery != null)
            return true;
        else
            return false;
    }

    public boolean isDeliveryReadyToSelectProducts(Delivery delivery) {
        if (delivery.pointOfDelivery != null)
            return true;
        else
            return false;
    }


    //
    // PointOfDelivery
    //
    public LiveData<List<PointOfDelivery>> getPointOfDeliveries() {
        return pointOfDeliveries;
    }


    public void onSelectedPointOfDelivery(PointOfDelivery selectedPointOfDelivery) {
        Delivery selectedDelivery = this.selectedDelivery.getValue();
        selectedDelivery.pointOfDelivery = selectedPointOfDelivery;
        this.updateSelectedDelivery();
    }

    public void onSelectedEmployee(Employee employee) {
        Delivery selectedDelivery = this.selectedDelivery.getValue();
        selectedDelivery.employee = employee;
        this.updateSelectedDelivery();

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
    //Employees
    //

    public LiveData<List<Employee>> getEmployees() {
        return employees;
    }

    public LiveData<Employee> getEmployeeByDefault() {

        return employeeByDefault;
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

        //By default apply PointOfDelivery discount
        BigDecimal discount = deliveryProductDetailToUpdate.discount == null ?
                deliveryProductDetailToUpdate.discount : selectedDelivery.getValue().pointOfDelivery.discountPercentage;
        BigDecimal discountPercentage = discount.divide(BigDecimal.valueOf(100));
        BigDecimal discountMultiplicator = BigDecimal.ONE.add(discountPercentage.negate());

        BigDecimal priceHtTot = deliveryProductDetailToUpdate.priceHtUnit.multiply(discountMultiplicator)
                .multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.CEILING);

        DeliveryProductsJoin dp = new DeliveryProductsJoin(deliveryProductDetailToUpdate.deliveryId,
                deliveryProductDetailToUpdate.productId,
                deliveryProductDetailToUpdate.type,
                quantity,
                deliveryProductDetailToUpdate.priceHtUnit,
                priceHtTot,
                deliveryProductDetailToUpdate.vat,
                discountPercentage
        );

        if (dp.quantity == 0) {
            mRepository.deleteDeliveryProductJoin(dp.deliveryId, dp.productId, dp.type);
        } else {
            mRepository.insertReplace(dp);
        }
    }


}