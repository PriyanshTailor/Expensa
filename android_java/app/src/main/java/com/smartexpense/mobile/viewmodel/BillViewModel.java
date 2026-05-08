package com.smartexpense.mobile.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.smartexpense.mobile.database.AppDatabase;
import com.smartexpense.mobile.model.Bill;

import java.util.List;

public class BillViewModel extends AndroidViewModel {

    private final LiveData<List<Bill>> allBills;
    private final LiveData<List<Bill>> unpaidBills;
    private final AppDatabase db;

    public BillViewModel(Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        allBills = db.billDao().getAllBills(uid);
        unpaidBills = db.billDao().getUnpaidBills(uid);
    }

    public LiveData<List<Bill>> getAllBills() {
        return allBills;
    }

    public LiveData<List<Bill>> getUnpaidBills() {
        return unpaidBills;
    }

    public void insert(Bill bill) {
        new Thread(() -> db.billDao().insert(bill)).start();
    }
    
    public void markAsPaid(int id) {
        new Thread(() -> db.billDao().markAsPaid(id)).start();
    }
}
