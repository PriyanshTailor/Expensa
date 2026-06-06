package com.smartexpense.mobile;

public class ExpenseModel {
    private String id;
    private double amount;
    private String category;
    private String paymentMode;
    private String type; // debit or credit
    private String merchant;
    private String note;
    private long date;

    public ExpenseModel() {} // Required for Firestore

    public ExpenseModel(double amount, String category, String paymentMode, String type, String merchant, String note, long date) {
        this.amount = amount;
        this.category = category;
        this.paymentMode = paymentMode;
        this.type = type;
        this.merchant = merchant;
        this.note = note;
        this.date = date;
    }

    // Getters and Seters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public String getPaymentMode() { return paymentMode; }
    public String getType() { return type; }
    public String getMerchant() { return merchant; }
    public String getNote() { return note; }
    public long getDate() { return date; }
}
