package com.smartexpense.mobile.sms;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParserUtil {

    private static final String AMOUNT_REGEX = "(?i)(?:rs\\.?|inr|₹)\\s?([\\d,]+\\.?\\d{0,2})";
    private static final String DEBIT_REGEX = "(?i)(debited|withdrawn|paid|spent|sent|transfer)";
    private static final String CREDIT_REGEX = "(?i)(credited|received|deposited|added)";
    private static final String MERCHANT_REGEX = "(?i)(?:to|at|from|for)\\s+([A-Za-z0-9 ]+?)(?:\\s+on|\\s+via|\\s+using|\\.|$)";

    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();

    static {
        CATEGORY_MAP.put("zomato|swiggy|restaurant|cafe|food", "Food & Dining");
        CATEGORY_MAP.put("uber|ola|rapido|petrol|fuel|metro", "Transport");
        CATEGORY_MAP.put("amazon|flipkart|myntra|meesho|nykaa", "Shopping");
        CATEGORY_MAP.put("netflix|hotstar|spotify|prime|bookmyshow", "Entertainment");
        CATEGORY_MAP.put("electricity|gas|broadband|airtel|jio|bsnl|bill", "Bills & Utilities");
        CATEGORY_MAP.put("pharmacy|apollo|hospital|doctor|medplus", "Health");
        CATEGORY_MAP.put("school|college|tuition|udemy|byju", "Education");
        CATEGORY_MAP.put("dmart|bigbasket|blinkit|zepto|grocery", "Groceries");
        CATEGORY_MAP.put("makemytrip|irctc|hotel|flight|railway", "Travel");
    }

    public static class ParsedTransaction {
        public double amount;
        public String type; // debit or credit
        public String merchant;
        public String category;
        public String originalMessage;

        public ParsedTransaction(double amount, String type, String merchant, String category, String originalMessage) {
            this.amount = amount;
            this.type = type;
            this.merchant = merchant;
            this.category = category;
            this.originalMessage = originalMessage;
        }
    }

    public static ParsedTransaction parse(String message) {
        double amount = 0;
        String type = "debit";
        String merchant = "Unknown Merchant";
        String category = "Other";

        // 1. Amount
        Pattern amountPattern = Pattern.compile(AMOUNT_REGEX);
        Matcher amountMatcher = amountPattern.matcher(message);
        if (amountMatcher.find()) {
            try {
                amount = Double.parseDouble(amountMatcher.group(1).replace(",", ""));
            } catch (Exception ignored) {}
        }

        // 2. Type
        if (message.toLowerCase().matches(".*" + CREDIT_REGEX + ".*")) {
            type = "credit";
        } else if (message.toLowerCase().matches(".*" + DEBIT_REGEX + ".*")) {
            type = "debit";
        }

        // 3. Merchant
        Pattern merchantPattern = Pattern.compile(MERCHANT_REGEX);
        Matcher merchantMatcher = merchantPattern.matcher(message);
        if (merchantMatcher.find()) {
            merchant = merchantMatcher.group(1).trim();
        }

        // 4. Category & Bill Detection
        for (Map.Entry<String, String> entry : CATEGORY_MAP.entrySet()) {
            if (message.toLowerCase().matches(".*(" + entry.getKey() + ").*")) {
                category = entry.getValue();
                break;
            }
        }
        
        // Explicit Bill Detection
        if (message.toLowerCase().contains("bill") || message.toLowerCase().contains("due")) {
            if (category.equals("Other")) category = "Bills & Utilities";
        }

        return new ParsedTransaction(amount, type, merchant, category, message);
    }
}
