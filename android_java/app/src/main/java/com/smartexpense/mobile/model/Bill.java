package com.smartexpense.mobile.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "bills")
public class Bill {
    @com.google.gson.annotations.SerializedName(value = "id", alternate = {"_id"})
    @PrimaryKey
    @androidx.annotation.NonNull
    public String id = "";
    
    public String billType;
    public String billerName;
    public double amountDue;
    public String dueDate;
    public boolean isPaid;
    public String rawSms;
    public long detectedAt;
    public String userId; // Link to FirebaseAuth UID

    public Bill() {}

    public Bill(String billType, String billerName, double amountDue, String dueDate, boolean isPaid, String rawSms, long detectedAt, String userId) {
        this.billType = billType;
        this.billerName = billerName;
        this.amountDue = amountDue;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
        this.rawSms = rawSms;
        this.detectedAt = detectedAt;
        this.userId = userId;
    }
}
