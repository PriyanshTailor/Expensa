import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;

public class TestRegex {
    private static final String AMOUNT_REGEX = "(?i)(?:rs\\.?|inr|₹)\\s?([\\d,]+\\.?\\d{0,2})";
    private static final String DEBIT_REGEX = "(?i)(debited|withdrawn|paid|spent|sent|transfer)";
    private static final String CREDIT_REGEX = "(?i)(credited|received|deposited|added)";
    private static final String MERCHANT_REGEX = "(?i)(?:to|at|from|for)\\s+([A-Za-z0-9 ]+?)(?:\\s+on|\\s+via|\\s+using|\\.|$)";

    public static void main(String[] args) {
        String[] messages = {
            "Rs. 450.00 has been debited from your A/C ending 1234. Paid to Zomato via UPI on 12-Oct-23.",
            "INR 5500.00 credited to your account ending 9999 from ABC Corp.",
            "Dear Customer, your electricity bill of Rs. 1250.00 for Adani Power is due on 15-Nov. Please pay to avoid late fees."
        };
        for(String m : messages) {
            System.out.println("Testing: " + m);
            Pattern amountPattern = Pattern.compile(AMOUNT_REGEX);
            Matcher amountMatcher = amountPattern.matcher(m);
            if(amountMatcher.find()){
                System.out.println("Amount: " + amountMatcher.group(1));
            } else {
                System.out.println("Failed Amount");
            }
        }
    }
}
