package com.smartexpense.mobile.sms;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.smartexpense.mobile.database.AppDatabase;
import com.smartexpense.mobile.model.Bill;
import com.smartexpense.mobile.model.Transaction;

public class SmsWorker extends Worker {

    public SmsWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String message = getInputData().getString("message");
        String sender = getInputData().getString("sender");
        long timestamp = getInputData().getLong("timestamp", System.currentTimeMillis());

        if (message == null || sender == null) {
            return Result.failure();
        }

        try {
            SmsParser.ParsedData parsedData = SmsParser.parseMessage(message, timestamp, message);
            if (parsedData != null) {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                
                if (parsedData.isBill()) {
                    Bill bill = parsedData.bill;
                    if (db.billDao().countByRawSms(bill.rawSms) == 0) {
                        db.billDao().insert(bill);
                        Log.d("SmsWorker", "Inserted new bill: " + bill.amountDue);
                    }
                } else {
                    Transaction transaction = parsedData.transaction;
                    // Check for duplicates
                    if (db.transactionDao().countByRawSms(transaction.rawSms) == 0) {
                        db.transactionDao().insert(transaction);
                        Log.d("SmsWorker", "Inserted new transaction: " + transaction.amount);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("SmsWorker", "Error processing SMS", e);
            return Result.failure();
        }

        return Result.success();
    }
}
