import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestParser {
    private static final String AMOUNT_REGEX = "(?:Rs\\.?|INR|₹)\\s*([\\d,]+\\.?\\d*)";
    private static final String DEBIT_REGEX = "(?i)\\b(debited|debit|withdrawn|spent|payment of|paid|sent|transfer|transferred)\\b";
    private static final String CREDIT_REGEX = "(?i)\\b(credited|credit|received|deposited|refund|added)\\b";
    private static final String ACCOUNT_REGEX = "(?i)(?:a/c|account|ac|card)[\\s*Xx]*(\\d{4})";
    private static final String BILL_KEYWORDS_REGEX = "(?i)\\b(bill|due|payment due|electricity|recharge|postpaid|broadband|emi|loan|insurance|subscription|netflix|jio|airtel|bsnl|water bill|gas bill|credit card bill)\\b";

    public static void main(String[] args) {
        String[] messages = {
            "Rs 1500.00 debited from a/c **1234 on 05/08 to ZOMATO. Avl Bal: Rs 500.00.",
            "Your a/c no. XXXXXX1234 is credited by Rs. 5,000.00 on 05-May-26",
            "Paid Rs. 500.00 to VPA zomato@upi",
            "Dear Customer, Rs.500.00 has been debited from your account **1234. Info: UPI/3123456789/Paytm.",
            "Dear user, your HDFC Bank a/c **1234 is debited by INR 1000.00 on 08-05. Avl Bal: INR 5000.00."
        };

        for (String message : messages) {
            System.out.println("Processing: " + message);
            
            Matcher amountMatcher = Pattern.compile(AMOUNT_REGEX, Pattern.CASE_INSENSITIVE).matcher(message);
            if (amountMatcher.find()) {
                System.out.println("  Amount Match: " + amountMatcher.group(1));
            } else {
                System.out.println("  FAILED TO FIND AMOUNT");
            }

            Matcher debitMatcher = Pattern.compile(DEBIT_REGEX).matcher(message);
            Matcher creditMatcher = Pattern.compile(CREDIT_REGEX).matcher(message);
            if (debitMatcher.find()) {
                System.out.println("  Type Match: DEBIT");
            } else if (creditMatcher.find()) {
                System.out.println("  Type Match: CREDIT");
            } else {
                System.out.println("  FAILED TO FIND TYPE");
            }
        }
    }
}
