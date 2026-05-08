package com.smartexpense.mobile.sms;

public class TransactionDetails {
    private double amount;
    private String type;
    private String originalMessage;
    private String category;
    private long timestamp;

    public TransactionDetails(double amount, String type, String originalMessage, String category) {
        this.amount = amount;
        this.type = type;
        this.originalMessage = originalMessage;
        this.category = category;
        this.timestamp = System.currentTimeMillis();
    }

    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getOriginalMessage() { return originalMessage; }
    public String getCategory() { return category; }
    public long getTimestamp() { return timestamp; }
}
