package com.nfrancoi.delivery.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nfrancoi.delivery.room.entities.Product;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface ProductDao {

    // allowing the insertReplace of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    Single<Long> insert(Product product);

    @Query("DELETE FROM Product")
    void deleteAll();

    @Query("SELECT * from Product where isActive = 1")
    LiveData<List<Product>> getProducts();

}