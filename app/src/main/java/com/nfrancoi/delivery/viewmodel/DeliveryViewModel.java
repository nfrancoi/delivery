package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Delivery;
import com.nfrancoi.delivery.room.entities.DeliveryProductsJoin;
import com.nfrancoi.delivery.room.entities.Employee;
import com.nfrancoi.delivery.room.entities.PointOfDelivery;
import com.nfrancoi.delivery.tools.CalendarTools;
import com.nfrancoi.delivery.tools.StringTools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

public class DeliveryViewModel extends AndroidViewModel {

    private Repository mRepository;

    //in constructor only
    private Long deliveryId;

    private LiveData<Delivery> selectedDelivery;

    private LiveData<List<PointOfDelivery>> pointOfDeliveries;
    private LiveData<List<Employee>> activeEmployees;
    private LiveData<Employee> employeeByDefault;


    //
    //cache
    //

    //Deposit
    private LiveData<List<DeliveryProductsJoin>> selectedDepositDeliveryProducts;
    private MutableLiveData<String> filterDepositLD;
    private MediatorLiveData<List<DeliveryProductsJoin>> filteredDepositDeliveryProducts;
    private MutableLiveData<Boolean> filterDepositZeroQuantity;

    //take
    private LiveData<List<DeliveryProductsJoin>> selectedTakeDeliveryProducts;
    private MutableLiveData<String> filterTakeLD;
    private MediatorLiveData<List<DeliveryProductsJoin>> filteredTakeDeliveryProducts;
    private MutableLiveData<Boolean> filterTakeZeroQuantity;


    //sell
    private LiveData<List<DeliveryProductsJoin>> selectedSellDeliveryProducts;

    private MediatorLiveData<BigDecimal> selectedDeliveryTotalAmount = new MediatorLiveData<>();


    //Edit DeliveryProductJoin
    private DeliveryProductsJoin editDeliveryProductsJoin;


    public DeliveryViewModel(Application application, Long deliveryId) {
        super(application);
        this.deliveryId = deliveryId;
        mRepository = Repository.getInstance();

        selectedDelivery = mRepository.getDelivery(deliveryId);
        activeEmployees = mRepository.getActiveEmployees();
        employeeByDefault = mRepository.getEmployeeByDefault();

        pointOfDeliveries = mRepository.getPointOfDeliveries();

        //
        // Deposit
        //
        this.selectedDepositDeliveryProducts = mRepository.loadDepositDeliveryProducts(deliveryId);
        this.filterDepositLD = new MutableLiveData<>("");
        this.filterDepositZeroQuantity = new MutableLiveData<>(false);

        this.filteredDepositDeliveryProducts = new MediatorLiveData<>();
        filteredDepositDeliveryProducts.addSource(this.selectedDepositDeliveryProducts, dpjs -> {
            String filterString = this.filterDepositLD.getValue();
            boolean filterZeroQuantity = this.filterDepositZeroQuantity.getValue();
            List<DeliveryProductsJoin> filteredDpjs = this.filter(dpjs, filterString, filterZeroQuantity);
            filteredDepositDeliveryProducts.setValue(filteredDpjs);
        });

        this.filteredDepositDeliveryProducts.addSource(this.filterDepositLD, filterString -> {
            List<DeliveryProductsJoin> dpjs = this.selectedDepositDeliveryProducts.getValue();
            boolean filterZeroQuantity = this.filterDepositZeroQuantity.getValue();
            List<DeliveryProductsJoin> filteredDpjs = this.filter(dpjs, filterString,filterZeroQuantity);
            filteredDepositDeliveryProducts.setValue(filteredDpjs);
        });
        this.filteredDepositDeliveryProducts.addSource(this.filterDepositZeroQuantity, filterZeroQuantity -> {
            String filterString = this.filterDepositLD.getValue();
            List<DeliveryProductsJoin> dpjs = this.selectedDepositDeliveryProducts.getValue();
            List<DeliveryProductsJoin> filteredDpjs = this.filter(dpjs, filterString,this.filterDepositZeroQuantity.getValue());
            filteredDepositDeliveryProducts.setValue(filteredDpjs);
        });


        //
        // Take
        //
        this.selectedTakeDeliveryProducts = mRepository.loadTakeDeliveryProducts(deliveryId);
        this.filterTakeLD = new MutableLiveData<>("");
        this.filterTakeZeroQuantity = new MutableLiveData<>(false);

        this.filteredTakeDeliveryProducts = new MediatorLiveData<>();
        filteredTakeDeliveryProducts.addSource(this.selectedTakeDeliveryProducts, dpjs -> {
            String filterString = this.filterTakeLD.getValue();
            boolean filterZeroQuantity = this.filterTakeZeroQuantity.getValue();
            List<DeliveryProductsJoin> filteredDpjs = this.filter(dpjs, filterString,filterZeroQuantity);
            filteredTakeDeliveryProducts.setValue(filteredDpjs);
        });

        this.filteredTakeDeliveryProducts.addSource(this.filterTakeLD, filterString -> {
            boolean filterZeroQuantity = this.filterTakeZeroQuantity.getValue();
            List<DeliveryProductsJoin> dpjs = this.selectedTakeDeliveryProducts.getValue();
            List<DeliveryProductsJoin> filteredDpjs = this.filter(dpjs, filterString, filterZeroQuantity);
            filteredTakeDeliveryProducts.setValue(filteredDpjs);
        });
        this.filteredTakeDeliveryProducts.addSource(this.filterTakeZeroQuantity, filterZeroQuantity -> {
            String filterString = this.filterTakeLD.getValue();
            List<DeliveryProductsJoin> dpjs = this.selectedTakeDeliveryProducts.getValue();
            List<DeliveryProductsJoin> filteredDpjs = this.filter(dpjs, filterString,this.filterTakeZeroQuantity.getValue());
            filteredTakeDeliveryProducts.setValue(filteredDpjs);
        });


        //
        // Sell
        //
        this.selectedSellDeliveryProducts = mRepository.loadSellDeliveryProducts(deliveryId);

        //
        //Total
        //
        this.selectedDeliveryTotalAmount = new MediatorLiveData<>();
        this.selectedDeliveryTotalAmount.setValue(BigDecimal.ZERO);

        this.selectedDeliveryTotalAmount.addSource(selectedDepositDeliveryProducts, this::totalAmountObserver);
        this.selectedDeliveryTotalAmount.addSource(selectedTakeDeliveryProducts, this::totalAmountObserver);
        this.selectedDeliveryTotalAmount.addSource(selectedSellDeliveryProducts, this::totalAmountObserver);

    }

    private List<DeliveryProductsJoin> filter(List<DeliveryProductsJoin> deliveryProducts, String filter, boolean filterZeroQuantity) {
        if (deliveryProducts == null) {
            return new ArrayList<>();
        }

        return deliveryProducts.stream()
                .filter(deliveryProduct -> (
                                //filter quantity if flag activated
                                (deliveryProduct.quantity != 0 || filterZeroQuantity == false)
                                        &&
                                        (
                                                deliveryProduct.productName.toLowerCase().contains(filter.toLowerCase())
                                                        || StringTools.PriceFormat.format(deliveryProduct.priceUnitVatIncl).contains(filter.toLowerCase())
                                        )
                        )
                ).collect(Collectors.toList());
    }


    public void setFilterDepositZeroQuantity(boolean filterDepositZeroQuantity){
        this.filterDepositZeroQuantity.setValue(filterDepositZeroQuantity);
    }


    public void setFilterTakeZeroQuantity(boolean filterTakeZeroQuantity){
        this.filterTakeZeroQuantity.setValue(filterTakeZeroQuantity);
    }

    public MutableLiveData<Boolean> getFilterDepositZeroQuantity() {
        return filterDepositZeroQuantity;
    }

    public MutableLiveData<Boolean> getFilterTakeZeroQuantity() {
        return filterTakeZeroQuantity;
    }

    //
    // Delivery
    //
    public LiveData<Delivery> getSelectedDelivery() {
        return selectedDelivery;
    }

    private String calculateNoteId(Employee employee) {
        return CalendarTools.YYYYMMDD.format(Calendar.getInstance().getTime()) + "_" + employee.notePrefix + selectedDelivery.getValue().deliveryId;
    }


    //
    // Deposit products
    //
    public void filterDepositDeliveryProductDetails(String filter) {
        this.filterDepositLD.setValue(filter);
    }

    public LiveData<String> getFilterDepositLD() {
        return filterDepositLD;
    }

    public LiveData<List<DeliveryProductsJoin>> getSelectedDepositDeliveryProducts() {
        return selectedDepositDeliveryProducts;
    }

    public LiveData<List<DeliveryProductsJoin>> getFilteredDepositDeliveryProducts() {
        return filteredDepositDeliveryProducts;
    }


    public void deleteDeliveryProductsJoin(DeliveryProductsJoin dpj) {
        mRepository.deleteDeliveryProductJoin(dpj);
    }


    //
    // Take
    //
    public void filterTakeDeliveryProductDetails(String filter) {
        this.filterTakeLD.setValue(filter);
    }

    public LiveData<String> getFilterTakeLD() {
        return filterTakeLD;
    }

    public LiveData<List<DeliveryProductsJoin>> getSelectedTakeDeliveryProducts() {
        return selectedTakeDeliveryProducts;
    }

    public LiveData<List<DeliveryProductsJoin>> getFilteredTakeDeliveryProducts() {
        return filteredTakeDeliveryProducts;
    }

    //
    // Sell
    //
    public LiveData<List<DeliveryProductsJoin>> getSelectedSellDeliveryProducts() {
        return selectedSellDeliveryProducts;
    }


    //
    // Total
    //

    public LiveData<BigDecimal> getSelectedDeliveryTotalAmount() {
        return selectedDeliveryTotalAmount;
    }

    private void totalAmountObserver(List<DeliveryProductsJoin> deliveryProductDetails) {
        BigDecimal sumPriceDeposit = BigDecimal.ZERO;
        if (selectedDepositDeliveryProducts.getValue() != null) {
            sumPriceDeposit = selectedDepositDeliveryProducts.getValue().stream().map(value -> value.priceUnitVatIncl.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        BigDecimal sumPriceTake = BigDecimal.ZERO;
        if (selectedTakeDeliveryProducts.getValue() != null) {
            sumPriceTake = selectedTakeDeliveryProducts.getValue().stream().map(value -> value.priceUnitVatIncl.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        BigDecimal sumPriceSell = BigDecimal.ZERO;
        if (selectedSellDeliveryProducts.getValue() != null) {
            sumPriceSell = selectedSellDeliveryProducts.getValue().stream().map(value -> value.priceUnitVatIncl.multiply(BigDecimal.valueOf(value.quantity))).reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        selectedDeliveryTotalAmount.setValue(sumPriceDeposit.add(sumPriceTake).add(sumPriceSell));
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
    //Employees
    //

    public LiveData<List<Employee>> getActiveEmployees() {
        return activeEmployees;
    }

    public LiveData<Employee> getEmployeeByDefault() {

        return employeeByDefault;
    }


    //
    // Save
    //

    public void updateSelectedDelivery() {
        Delivery currentDelivery = selectedDelivery.getValue();
        mRepository.updateSync(currentDelivery);
    }

    public void updateDeliverySync(Delivery delivery) {
        mRepository.updateSync(delivery);
    }


    public LiveData<Delivery> updateDelivery(Delivery delivery) {
        return mRepository.update(delivery);
    }


    public void saveProductDetail(DeliveryProductsJoin deliveryProductDetailToUpdate) {
        if (selectedDepositDeliveryProducts == null)
            throw new IllegalStateException("selectedDepositDeliveryProductDetails MUST not be null");


        if (deliveryProductDetailToUpdate.quantity == 0) {
            mRepository.deleteDeliveryProductJoin(deliveryProductDetailToUpdate);
        } else {
            //Capture Quantity / PU vatIncl / VAT

            //By default apply PointOfDelivery discount
            BigDecimal discount = deliveryProductDetailToUpdate.discount != null ?
                    deliveryProductDetailToUpdate.discount : selectedDelivery.getValue().pointOfDelivery.discountPercentage;
            deliveryProductDetailToUpdate.discount = discount;

            //negate quantity for return
            int quantity = deliveryProductDetailToUpdate.quantity * ("T".equals(deliveryProductDetailToUpdate.type) ? -1 : 1);
            deliveryProductDetailToUpdate.quantity = quantity;

            mRepository.insertReplace(deliveryProductDetailToUpdate);
        }
    }


    //
    // Edit DeliveryProductsJoin
    //
    public DeliveryProductsJoin getEditDeliveryProductsJoin() {
        return editDeliveryProductsJoin;
    }

    public void setEditDeliveryProductsJoin(DeliveryProductsJoin editDeliveryProductsJoin) {
        this.editDeliveryProductsJoin = editDeliveryProductsJoin;
    }


}