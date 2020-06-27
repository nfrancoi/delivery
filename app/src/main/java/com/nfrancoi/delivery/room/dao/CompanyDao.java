package com.nfrancoi.delivery.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.nfrancoi.delivery.room.entities.Company;

@Dao
public abstract class CompanyDao extends BaseDao<Company>{

    // allowing the insertReplace of the same word multiple times by passing a
    // conflict resolution strategy

    @Query("SELECT * from Company where  companyId= 1")
    public abstract LiveData<Company> getFirstCompany();

    @Query("DELETE FROM Company")
    abstract public void deleteAll();

}