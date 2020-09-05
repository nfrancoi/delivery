package com.nfrancoi.delivery.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import com.nfrancoi.delivery.room.entities.Company;

import java.util.List;

@Dao
public abstract class CompanyDao extends BaseDao<Company>{

    // allowing the insertReplace of the same word multiple times by passing a
    // conflict resolution strategy

    @Query("SELECT * from Company where  companyId= 1")
    public abstract LiveData<Company> getFirstCompany();

    @Query("SELECT count(*) from Company")
    public abstract int countSync();

    @Query("SELECT * from Company order by companyId asc")
    public abstract List<Company> getAllSync();

    @Query("DELETE FROM Company")
    abstract public void deleteAll();


}