package com.smartexpense.mobile.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.smartexpense.mobile.database.AppDatabase;
import com.smartexpense.mobile.model.Bill;
import com.smartexpense.mobile.model.Transaction;

import java.util.List;

public class ExpenseViewModel extends AndroidViewModel {

    private final LiveData<List<Transaction>> allTransactions;
    private final LiveData<List<Bill>> allBills;
    private final AppDatabase db;

    public ExpenseViewModel(Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        allTransactions = db.transactionDao().getAllTransactions(uid);
        allBills = db.billDao().getAllBills(uid);
    }

    public LiveData<List<Transaction>> getAllTransactions() {
        return allTransactions;
    }

    public LiveData<List<Bill>> getAllBills() {
        return allBills;
    }

    public void insert(Transaction transaction) {
        new Thread(() -> db.transactionDao().insert(transaction)).start();
    }
}
