package com.smartexpense.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill {
    @Id
    private String id;
    private String userId;
    private String billType;
    private String billerName;
    private double amountDue;
    private String dueDate;
    private boolean isPaid;
    private String rawSms;
    private long detectedAt;
}
