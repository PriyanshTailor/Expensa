package com.smartexpense.mobile.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.smartexpense.mobile.model.Transaction;

import java.util.List;

@Dao
public interface TransactionDao {
    @Insert
    long insert(Transaction transaction);

    @Update
    void update(Transaction transaction);

    @Delete
    void delete(Transaction transaction);

    @Query("SELECT * FROM transactions WHERE source = 'SMS' OR userId = :uid ORDER BY timestamp DESC")
    LiveData<List<Transaction>> getAllTransactions(String uid);

    @Query("SELECT * FROM transactions WHERE pendingReview = 1 ORDER BY timestamp DESC")
    List<Transaction> getPendingTransactionsSync();

    @Query("SELECT * FROM transactions WHERE pendingReview = 1 ORDER BY timestamp DESC")
    LiveData<List<Transaction>> getPendingTransactions();

    @Query("UPDATE transactions SET pendingReview = 0 WHERE id IN (:ids)")
    void markAsReviewed(List<Integer> ids);

    @Query("SELECT COUNT(*) FROM transactions WHERE rawSms = :rawSms")
    int countByRawSms(String rawSms);
    
    @Query("SELECT * FROM transactions WHERE rawSms = :rawSms LIMIT 1")
    Transaction getByRawSms(String rawSms);
}
