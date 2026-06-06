package com.smartexpense.mobile;

import java.util.Date;

public class BillModel {

    private String billerName;
    private double amountDue;
    private Date dueDate;
    private boolean isPaid;
    private String category;
    private Date lastPaidAt;

    public BillModel() {
        // Required empty public constructor for Firestore
    }

    public BillModel(String billerName, double amountDue, Date dueDate, boolean isPaid, String category) {
        this.billerName = billerName;
        this.amountDue = amountDue;
        this.dueDate = dueDate;
        this.isPaid = isPaid;
        this.category = category;
    }

    // Getters and Setters
    public String getBillerName() { return billerName; }
    public void setBillerName(String billerName) { this.billerName = billerName; }

    public double getAmountDue() { return amountDue; }
    public void setAmountDue(double amountDue) { this.amountDue = amountDue; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public boolean isPaid() { return isPaid; }
    public void setPaid(boolean paid) { isPaid = paid; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Date getLastPaidAt() { return lastPaidAt; }
    public void setLastPaidAt(Date lastPaidAt) { this.lastPaidAt = lastPaidAt; }
}
