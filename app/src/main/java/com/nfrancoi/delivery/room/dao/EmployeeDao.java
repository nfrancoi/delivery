package com.nfrancoi.delivery.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.nfrancoi.delivery.room.entities.Employee;

import java.util.List;

import io.reactivex.Single;

@Dao
public interface EmployeeDao {

    // allowing the insertReplace of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Single<Long> insert(Employee employee);

    @Query("SELECT * from Employee")
    LiveData<List<Employee>> getEmployee();

    @Query("DELETE FROM Employee")
    void deleteAll();
}