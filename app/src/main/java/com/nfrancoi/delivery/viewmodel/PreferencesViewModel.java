package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Company;
import com.nfrancoi.delivery.room.entities.Employee;
import com.nfrancoi.delivery.room.entities.Product;

import java.util.List;

public class PreferencesViewModel extends AndroidViewModel {

    private Repository mRepository;
    private LiveData<List<Product>> products;
    private LiveData<Company> company;
    private LiveData<List<Employee>> employees;

    public PreferencesViewModel(Application application) {
        super(application);
        mRepository = Repository.getInstance();
        products = mRepository.getProducts();
        company = mRepository.getFirstCompany();
        employees = mRepository.getActiveEmployees();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    //
    // Company
    //
    public LiveData<Company> getCompany() {
        return company;
    }

    public void update(Company company) {
        mRepository.updateSync(company);
    }

    public LiveData<List<Employee>> getEmployees() {
        return employees;
    }
}