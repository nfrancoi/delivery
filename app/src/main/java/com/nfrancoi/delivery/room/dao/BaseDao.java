package com.nfrancoi.delivery.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class BaseDao<T> {


    public List<T> getAllSync(){
        throw new IllegalStateException(" method getAllSync() must be overwrited");
    };


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract Single<Long> insert(T obj);

    @Insert(onConflict = OnConflictStrategy.ABORT)
    public abstract Long insertSync(T obj);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract Single<Long> insertReplace(T obj);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract Long insertReplaceSync(T obj);

    /**
     * Insert an array of objects in the database.
     *
     * @param obj the objects to be inserted.
     * @return The SQLite row ids
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract List<Long> insertReplace(List<T> obj);

    /**
     * Update an object from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    public abstract int updateSync(T obj);

    /**
     * Update an array of objects from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    public abstract int updateSync(List<T> obj);

    /**
     * Delete an object from the database
     *
     * @param obj the object to be deleted
     */
    @Delete
    public abstract int deleteSync(T obj);


}
