package com.nfrancoi.delivery.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class BaseDao<T> {


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    public abstract Single<Long> insert(T obj);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract Single<Long> insertReplace(T obj);


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
    public abstract int update(T obj);

    /**
     * Update an array of objects from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    public abstract int update(List<T> obj);

    /**
     * Delete an object from the database
     *
     * @param obj the object to be deleted
     */
    @Delete
    public abstract void delete(T obj);


    @Transaction
    public void upsert(List<T> objList) {
        List<Long> insertResult = insertReplace(objList);
        List<T> updateList = new ArrayList<>();

        for (int i = 0; i < insertResult.size(); i++) {
            if (insertResult.get(i) == -1) {
                updateList.add(objList.get(i));
            }
        }

        if (!updateList.isEmpty()) {
            update(updateList);
        }
    }
}
