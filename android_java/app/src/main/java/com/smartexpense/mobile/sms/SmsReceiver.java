package com.smartexpense.mobile.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;





import java.util.HashMap;
import java.util.Map;

public class SmsReceiver extends BroadcastReceiver {
    private static final String TAG = "SmsReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null) {
                for (Object pdu : pdus) {
                    SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
                    String messageBody = smsMessage.getMessageBody();
                    
                    Toast.makeText(context, "SmsReceiver Triggered!", Toast.LENGTH_SHORT).show();

                    // Parse the SMS for transaction details using the new Util
                    SmsParserUtil.ParsedTransaction details = SmsParserUtil.parse(messageBody);
                    
                    if (details.amount > 0) {
                        final PendingResult pendingResult = goAsync();
                        new Thread(() -> {
                            try {
                                android.content.SharedPreferences prefs = context.getSharedPreferences("ExpenseTracker", Context.MODE_PRIVATE);
                                String uid = prefs.getString("userId", null);
                                
                                if (uid != null) {
                                    saveToFirestore(uid, details, smsMessage.getTimestampMillis());
                                } else {
                                    Log.e(TAG, "No user logged in, cannot save SMS.");
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error in SmsReceiver background processing", e);
                            } finally {
                                pendingResult.finish();
                            }
                        }).start();
                    }
                }
            }
        }
    }

    private void saveToFirestore(String uid, SmsParserUtil.ParsedTransaction tx, long timestamp) {
        
        boolean isBill = tx.originalMessage != null && (tx.originalMessage.toLowerCase().contains("due") || tx.originalMessage.toLowerCase().contains("bill") || "Bills & Utilities".equals(tx.category));

        Map<String, Object> data = new HashMap<>();
        data.put("amount", tx.amount);
        data.put("merchant", tx.merchant);
        data.put("category", tx.category);
        data.put("type", tx.type);
        data.put("date", timestamp);
        data.put("source", "sms_auto");
        data.put("rawMessage", tx.originalMessage);

        String docId = "sms_" + Math.abs((tx.originalMessage + timestamp).hashCode());
        
        String targetCollection = isBill ? "bills" : "expenses";
        
        // Replaced Firestore with Retrofit API
        try {
            if (!isBill) {
                com.smartexpense.mobile.network.RetrofitClient.getClient()
                    .create(com.smartexpense.mobile.network.ApiService.class)
                    .createTransaction(data)
                    .execute();
            } else {
                com.smartexpense.mobile.model.Bill bill = new com.smartexpense.mobile.model.Bill();
                bill.userId = uid;
                bill.billerName = tx.merchant != null ? tx.merchant : "Unknown Biller";
                bill.amountDue = tx.amount;
                bill.dueDate = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date(timestamp));
                bill.billType = tx.category != null ? tx.category : "Bills & Utilities";
                bill.isPaid = false;
                bill.rawSms = tx.originalMessage;
                bill.detectedAt = timestamp;

                com.smartexpense.mobile.network.RetrofitClient.getClient()
                    .create(com.smartexpense.mobile.network.ApiService.class)
                    .createBill(bill)
                    .execute();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to save SMS data to backend via Retrofit", e);
        }
    }
}
