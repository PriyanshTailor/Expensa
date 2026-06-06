package com.smartexpense.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    private String id;
    private String userId;
    private String type; // expense or income
    private String title;
    private String category;
    private double amount;
    private String note;
    private String merchant;
    private Long date;
    private String rawMessage;
    private String receiptUrl;
    private LocalDateTime transactionDate;
    private boolean isRecurring = false;
    private String source = "manual";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
