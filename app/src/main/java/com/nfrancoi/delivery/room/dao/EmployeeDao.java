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
public abstract class EmployeeDao extends BaseDao<Employee> {


    @Query("SELECT * from Employee order by employeeId asc")
    public abstract List<Employee> getAllSync();

    // allowing the insertReplace of the same word multiple times by passing a
    // conflict resolution strategy
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract Single<Long> insert(Employee employee);

    @Query("SELECT * FROM Employee ORDER BY name")
    public abstract LiveData<List<Employee>> getEmployee();

    @Query("SELECT * FROM Employee WHERE isDefault = 1")
    public abstract LiveData<Employee> getEmployeeByDefault();

    @Query("DELETE FROM Employee")
    public abstract void deleteAll();

}