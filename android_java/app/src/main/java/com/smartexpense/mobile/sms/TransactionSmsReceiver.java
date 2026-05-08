package com.smartexpense.mobile.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.smartexpense.mobile.database.AppDatabase;
import com.smartexpense.mobile.model.Bill;
import com.smartexpense.mobile.model.Transaction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TransactionSmsReceiver extends BroadcastReceiver {

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String BANK_SENDER_REGEX = "(?i)(.*HDFCBK.*|.*SBIINB.*|.*ICICIB.*|.*AXISBK.*|.*KOTAKB.*|.*BOI.*|.*PNB.*)";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(SMS_RECEIVED)) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                if (pdus != null) {
                    for (Object pdu : pdus) {
                        SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                        String sender = smsMessage.getDisplayOriginatingAddress();
                        String messageBody = smsMessage.getMessageBody();
                        long timestamp = smsMessage.getTimestampMillis();

                        Log.d("SmsReceiver", "Received SMS from: " + sender);

                        // For reliable detection, we now pass the message to SmsWorker 
                        // and let the robust SmsParser decide if it contains valid transaction data.
                        // This also allows for testing via emulators or personal numbers.
                        
                        final PendingResult pendingResult = goAsync();
                        new Thread(() -> {
                            try {
                                SmsParser.ParsedData parsedData = SmsParser.parseMessage(messageBody, timestamp, messageBody);
                                if (parsedData != null) {
                                    AppDatabase db = AppDatabase.getDatabase(context);
                                    if (parsedData.isBill()) {
                                        if (db.billDao().countByRawSms(parsedData.bill.rawSms) == 0) {
                                            db.billDao().insert(parsedData.bill);
                                        }
                                    } else {
                                        if (db.transactionDao().countByRawSms(parsedData.transaction.rawSms) == 0) {
                                            db.transactionDao().insert(parsedData.transaction);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e("SmsReceiver", "Error parsing SMS", e);
                            } finally {
                                pendingResult.finish();
                            }
                        }).start();
                    }
                }
            }
        }
    }
}
