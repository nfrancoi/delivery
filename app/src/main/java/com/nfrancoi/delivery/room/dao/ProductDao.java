package com.nfrancoi.delivery.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.nfrancoi.delivery.room.entities.Product;

import java.util.List;

@Dao
public abstract class ProductDao extends BaseDao<Product>{

    // allowing the insertReplace of the same word multiple times by passing a
    // conflict resolution strategy

    @Query("SELECT * from Product order by productId asc")
    public abstract List<Product> getAllSync();


    @Query("SELECT * from Product where isActive = 1")
    public abstract LiveData<List<Product>> getActiveProducts();


    @Query("DELETE FROM Product")
    public abstract void deleteAll();
}