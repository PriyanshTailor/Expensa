package com.smartexpense.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection = "trips")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trip {
    @Id
    private String id;
    private String name;
    private String createdBy;
    private Map<String, Double> members; // Member name -> Total amount spent
    private List<TripExpense> expenses; // List of all expenses
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
    private String destination; // Trip destination
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public Trip(String name, String createdBy) {
        this.name = name;
        this.createdBy = createdBy;
        this.members = new HashMap<>();
        this.expenses = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TripExpense {
        private String id;
        private String memberName;
        private Double amount;
        private String description;
        private LocalDateTime createdAt;
        private String category; // Food, Transport, etc.
    }
}
