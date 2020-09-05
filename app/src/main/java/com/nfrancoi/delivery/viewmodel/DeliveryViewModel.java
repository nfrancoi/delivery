package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;
import com.nfrancoi.delivery.room.entities.Employee;
import com.nfrancoi.delivery.room.entities.PointOfDelivery;
import com.nfrancoi.delivery.tools.CalendarTools;
import com.nfrancoi.delivery.tools.StringTools;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
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
    private LiveData<List<DeliveryProductsJoin>> selectedDepositDeliveryProductDetails;
    private MutableLiveData<String> filterDepositDeliveryProductDetails;
    private LiveData<List<DeliveryProductsJoin>> filteredDepositDeliveryProductDetails;

    private LiveData<List<DeliveryProductsJoin>> selectedTakeDeliveryProductDetails;
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
                List<DeliveryProductsJoin> listFiltered =
                        selectedDepositDeliveryProductDetails.getValue().stream()
                                .filter(deliveryProductDetail -> deliveryProductDetail.productName.toLowerCase().contains(filter.toLowerCase())
                                                                || StringTools.PriceFormat.format(deliveryProductDetail.priceUnitVatIncl).contains(filter.toLowerCase()))
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

    private String calculateNoteId(Employee employee){
        return CalendarTools.YYYYMMDD.format(Calendar.getInstance().getTime()) + "_" + employee.notePrefix + selectedDelivery.getValue().deliveryId;
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

    public LiveData<List<DeliveryProductsJoin>> getSelectedDepositDeliveryProductDetails() {
        return selectedDepositDeliveryProductDetails;
    }


    private void totalAmountObserver(List<DeliveryProductsJoin> deliveryProductDetails) {
        BigDecimal sumPriceDeposit = BigDecimal.ZERO;
        BigDecimal sumPriceTake = BigDecimal.ZERO;
        if (selectedDepositDeliveryProductDetails.getValue() != null) {
            sumPriceDeposit = selectedDepositDeliveryProductDetails.getValue().stream().map(value -> value.priceUnitVatIncl.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        if (selectedTakeDeliveryProductDetails.getValue() != null) {
            sumPriceTake = selectedTakeDeliveryProductDetails.getValue().stream().map(value -> value.priceUnitVatIncl.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        selectedDeliveryTotalAmount.setValue(sumPriceDeposit.add(sumPriceTake));
    }

    public LiveData<List<DeliveryProductsJoin>> getFilteredDepositDeliveryProductDetails() {
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
        selectedDelivery.noteId = this.calculateNoteId(employee);
        this.updateSelectedDelivery();

    }


    //
    // Take
    //
    public LiveData<List<DeliveryProductsJoin>> getSelectedTakeDeliveryProductDetails() {
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

    public void saveProductDetail(DeliveryProductsJoin deliveryProductDetailToUpdate) {
        if (selectedDepositDeliveryProductDetails == null)
            throw new IllegalStateException("selectedDepositDeliveryProductDetails MUST not be null");


        if (deliveryProductDetailToUpdate.quantity == 0) {
            mRepository.deleteDeliveryProductJoin(deliveryProductDetailToUpdate.deliveryId, deliveryProductDetailToUpdate.productId, deliveryProductDetailToUpdate.type);
        } else {
            //Capture Quantity and PU vatIncl

            //By default apply PointOfDelivery discount
            BigDecimal discount = deliveryProductDetailToUpdate.discount == null ?
                    deliveryProductDetailToUpdate.discount : selectedDelivery.getValue().pointOfDelivery.discountPercentage;
            deliveryProductDetailToUpdate.discount = discount;

            //negate quantity for return
            int quantity = deliveryProductDetailToUpdate.quantity * ("T".equals(deliveryProductDetailToUpdate.type) ? -1 : 1);
            deliveryProductDetailToUpdate.quantity = quantity;

            BigDecimal vatDivider = deliveryProductDetailToUpdate.vat.equals(BigDecimal.ZERO)? BigDecimal.ONE:BigDecimal.ONE.add(deliveryProductDetailToUpdate.vat.divide(BigDecimal.valueOf(100l)));
            BigDecimal priceUnitVatExcl = deliveryProductDetailToUpdate.priceUnitVatIncl.setScale(3).divide(vatDivider,RoundingMode.HALF_UP);
            deliveryProductDetailToUpdate.priceUnitVatExcl = priceUnitVatExcl.setScale(2, RoundingMode.HALF_UP);

            BigDecimal discountMultiplicator = BigDecimal.ONE.add(discount.negate().divide(BigDecimal.valueOf(100l)));
            BigDecimal priceTotVatExclDiscounted = priceUnitVatExcl.multiply(BigDecimal.valueOf(deliveryProductDetailToUpdate.quantity)).multiply(discountMultiplicator);
            deliveryProductDetailToUpdate.priceTotVatDiscounted = priceTotVatExclDiscounted.setScale(2, RoundingMode.HALF_UP);;



            mRepository.insertReplace(deliveryProductDetailToUpdate);
        }
    }


}