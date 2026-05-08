package com.smartexpense.mobile.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "transactions")
public class Transaction {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String type; // DEBIT or CREDIT
    public double amount;
    public String accountLast4;
    public String merchant;
    public String category; // Auto-categorized
    public long timestamp;
    public String rawSms;
    public boolean pendingReview;
    public String source; // "SMS" or "MANUAL"
    public String userId; // Link to FirebaseAuth UID for manual entries

    public Transaction() {}
    
    public Transaction(String type, double amount, String accountLast4, String merchant, String category, long timestamp, String rawSms, boolean pendingReview, String source, String userId) {
        this.type = type;
        this.amount = amount;
        this.accountLast4 = accountLast4;
        this.merchant = merchant;
        this.category = category;
        this.timestamp = timestamp;
        this.rawSms = rawSms;
        this.pendingReview = pendingReview;
        this.source = source;
        this.userId = userId;
    }
}
