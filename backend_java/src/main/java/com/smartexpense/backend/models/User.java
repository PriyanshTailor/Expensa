package com.smartexpense.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private String passwordHash;
    private String authProvider = "local";
    private String currency = "INR";
    private String avatarUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private Double savingsBalance = 0.0;
    private Double mutualFunds = 0.0;
}
