package com.smartexpense.mobile.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

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
                        new Thread(() -> {
                            try {
                                // Give Firebase Auth up to 2 seconds to initialize from disk if background killed
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                int retries = 0;
                                while (user == null && retries < 4) {
                                    Thread.sleep(500);
                                    user = FirebaseAuth.getInstance().getCurrentUser();
                                    retries++;
                                }
                                
                                if (user != null) {
                                    saveToFirestore(user.getUid(), details, smsMessage.getTimestampMillis());
                                } else {
                                    Log.e(TAG, "Auth failed in background, cannot save SMS.");
                                }
                            } catch (Exception e) {}
                        }).start();
                    }
                }
            }
        }
    }

    private void saveToFirestore(String uid, SmsParserUtil.ParsedTransaction tx, long timestamp) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        boolean isBill = tx.originalMessage != null && (tx.originalMessage.toLowerCase().contains("due") || tx.originalMessage.toLowerCase().contains("bill") || "Bills & Utilities".equals(tx.category));

        Map<String, Object> data = new HashMap<>();
        data.put("amount", tx.amount);
        data.put("merchant", tx.merchant);
        data.put("category", tx.category);
        data.put("type", tx.type);
        data.put("date", new com.google.firebase.Timestamp(new java.util.Date(timestamp)));
        data.put("source", "sms_auto");
        data.put("rawMessage", tx.originalMessage);

        String docId = "sms_" + Math.abs((tx.originalMessage + timestamp).hashCode());
        
        String targetCollection = isBill ? "bills" : "expenses";
        
        db.collection("users").document(uid).collection(targetCollection).document(docId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                com.google.firebase.firestore.DocumentSnapshot doc = task.getResult();
                if (doc != null && !doc.exists()) {
                    if (isBill) {
                        Map<String, Object> billData = new HashMap<>();
                        billData.put("name", tx.merchant);
                        billData.put("category", tx.category);
                        billData.put("amount", tx.amount);
                        billData.put("status", "upcoming");
                        billData.put("dueDay", java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH));
                        billData.put("frequency", "monthly");
                        db.collection("users").document(uid).collection("bills").document(docId).set(billData);
                    } else {
                        db.collection("users").document(uid).collection("expenses").document(docId).set(data);
                    }
                    
                    db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                        Double bal = userDoc.getDouble("savingsBalance");
                        if (bal == null) bal = 0.0;
                        if ("debit".equals(tx.type)) bal -= tx.amount;
                        else bal += tx.amount;
                        db.collection("users").document(uid).update("savingsBalance", bal);
                    });
                }
            } else {
                Log.e(TAG, "Error checking collection", task.getException());
            }
        });
    }
}
