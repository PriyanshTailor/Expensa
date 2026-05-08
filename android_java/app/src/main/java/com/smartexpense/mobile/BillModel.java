package com.smartexpense.mobile;

import com.google.firebase.Timestamp;

public class BillModel {
    private String id;
    private String name;
    private String category;
    private double amount;
    private int dueDay;
    private String frequency;
    private String status; // overdue, dueSoon, upcoming, paid
    private Timestamp lastPaidAt;

    public BillModel() {}

    public BillModel(String name, String category, double amount, int dueDay, String frequency) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.dueDay = dueDay;
        this.frequency = frequency;
        this.status = "upcoming";
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public int getDueDay() { return dueDay; }
    public String getFrequency() { return frequency; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getLastPaidAt() { return lastPaidAt; }
    public void setLastPaidAt(Timestamp lastPaidAt) { this.lastPaidAt = lastPaidAt; }
}
