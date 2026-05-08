package com.smartexpense.mobile.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.smartexpense.mobile.model.Bill;

import java.util.List;

@Dao
public interface BillDao {
    @Insert
    long insert(Bill bill);

    @Update
    void update(Bill bill);

    @Delete
    void delete(Bill bill);

    @Query("SELECT * FROM bills WHERE userId = :uid OR rawSms IS NOT NULL ORDER BY detectedAt DESC")
    LiveData<List<Bill>> getAllBills(String uid);

    @Query("SELECT * FROM bills WHERE (userId = :uid OR rawSms IS NOT NULL) AND isPaid = 0 ORDER BY dueDate ASC")
    LiveData<List<Bill>> getUnpaidBills(String uid);

    @Query("SELECT COUNT(*) FROM bills WHERE rawSms = :rawSms")
    int countByRawSms(String rawSms);
    
    @Query("UPDATE bills SET isPaid = 1 WHERE id = :id")
    void markAsPaid(int id);
}
