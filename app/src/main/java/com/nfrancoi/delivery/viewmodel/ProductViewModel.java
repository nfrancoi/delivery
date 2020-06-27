package com.nfrancoi.delivery.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.nfrancoi.delivery.repository.Repository;
import com.nfrancoi.delivery.room.entities.Product;

import java.util.List;

public class ProductViewModel extends AndroidViewModel {

    private Repository mRepository;
    private LiveData<List<Product>> products;

    public ProductViewModel(Application application) {
        super(application);
        mRepository = Repository.getInstance(application);
        products = mRepository.getProducts() ;
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

}