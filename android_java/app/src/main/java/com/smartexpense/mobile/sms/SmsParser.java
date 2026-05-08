package com.smartexpense.mobile.sms;

import com.smartexpense.mobile.model.Bill;
import com.smartexpense.mobile.model.Transaction;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsParser {

    private static final String AMOUNT_REGEX = "(?:Rs\\.?|INR|₹)\\s*([\\d,]+\\.?\\d*)";
    private static final String DEBIT_REGEX = "(?i)\\b(debited|debit|withdrawn|spent|payment of|paid|sent|transfer|transferred)\\b";
    private static final String CREDIT_REGEX = "(?i)\\b(credited|credit|received|deposited|refund|added)\\b";
    private static final String ACCOUNT_REGEX = "(?i)(?:a/c|account|ac|card)[\\s*Xx]*(\\d{4})";
    private static final String BALANCE_REGEX = "(?i)(?:bal(?:ance)?|avl bal)[:\\s]+(?:Rs\\.?|INR|₹)?\\s?([\\d,]+\\.?\\d*)";
    
    private static final String BILL_KEYWORDS_REGEX = "(?i)\\b(bill|due|payment due|electricity|recharge|postpaid|broadband|emi|loan|insurance|subscription|netflix|jio|airtel|bsnl|water bill|gas bill|credit card bill)\\b";

    public static class ParsedData {
        public Transaction transaction;
        public Bill bill;
        public boolean isBill() { return bill != null; }
    }

    public static ParsedData parseMessage(String message, long timestamp, String rawSms) {
        // First check if it's OTP or failed transaction
        if (message.toLowerCase().contains("otp") || message.toLowerCase().contains("failed") || message.toLowerCase().contains("unsuccessful")) {
            return null; // Ignore
        }

        double amount = 0;
        Pattern amountPattern = Pattern.compile(AMOUNT_REGEX, Pattern.CASE_INSENSITIVE);
        Matcher amountMatcher = amountPattern.matcher(message);
        if (amountMatcher.find()) {
            try {
                String amountStr = amountMatcher.group(1).replace(",", "");
                amount = Double.parseDouble(amountStr);
            } catch (Exception e) {
                return null;
            }
        } else {
            return null; // No amount found
        }

        String accountLast4 = "";
        Pattern accountPattern = Pattern.compile(ACCOUNT_REGEX);
        Matcher accountMatcher = accountPattern.matcher(message);
        if (accountMatcher.find()) {
            accountLast4 = accountMatcher.group(1);
        }

        boolean isBill = false;
        Matcher billMatcher = Pattern.compile(BILL_KEYWORDS_REGEX).matcher(message);
        if (billMatcher.find()) {
            isBill = true;
        }

        if (isBill) {
            Bill b = new Bill();
            b.amountDue = amount;
            b.rawSms = rawSms;
            b.detectedAt = timestamp;
            b.isPaid = false;
            b.billerName = extractMerchant(message);
            b.billType = "Utility"; // Could enhance to parse specific bill types
            b.dueDate = "Soon"; // Could enhance parsing date
            
            ParsedData result = new ParsedData();
            result.bill = b;
            return result;
        } else {
            String type = "";
            if (Pattern.compile(DEBIT_REGEX).matcher(message).find()) {
                type = "DEBIT";
            } else if (Pattern.compile(CREDIT_REGEX).matcher(message).find()) {
                type = "CREDIT";
            } else {
                return null; // Neither debit nor credit clearly found
            }

            Transaction t = new Transaction();
            t.type = type;
            t.amount = amount;
            t.accountLast4 = accountLast4;
            t.rawSms = rawSms;
            t.timestamp = timestamp;
            t.source = "SMS";
            t.pendingReview = true;
            t.merchant = extractMerchant(message);
            t.category = autoCategorize(t.merchant, message);

            ParsedData result = new ParsedData();
            result.transaction = t;
            return result;
        }
    }

    private static String extractMerchant(String message) {
        // A simple heuristic: everything after "at" or "to" or "info"
        Pattern p = Pattern.compile("(?i)(?:at|to|info)[\\s:]+([A-Za-z0-9\\s]+)");
        Matcher m = p.matcher(message);
        if (m.find()) {
            String match = m.group(1).trim();
            if (match.length() > 20) return match.substring(0, 20); // truncate
            return match;
        }
        return "Unknown";
    }

    private static String autoCategorize(String merchant, String message) {
        String m = (merchant + " " + message).toLowerCase();
        if (m.matches(".*\\b(zomato|swiggy|restaurant|food)\\b.*")) return "Food";
        if (m.matches(".*\\b(uber|ola|metro|rapido|irctc|flight|travel)\\b.*")) return "Transport";
        if (m.matches(".*\\b(amazon|flipkart|myntra|ajio|shopping|mart)\\b.*")) return "Shopping";
        if (m.matches(".*\\b(netflix|spotify|prime|movie|pvr)\\b.*")) return "Entertainment";
        if (m.matches(".*\\b(hospital|pharmacy|apollo|health|clinic)\\b.*")) return "Health";
        if (m.matches(".*\\b(school|college|coursera|education|fee)\\b.*")) return "Education";
        if (m.matches(".*\\b(electricity|water|gas|utility|bill)\\b.*")) return "Utilities";
        if (m.matches(".*\\b(atm|cash|withdrawal)\\b.*")) return "Cash Withdrawal";
        return "Others";
    }
}
