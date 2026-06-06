package com.smartexpense.backend.controllers;

import com.smartexpense.backend.models.Transaction;
import com.smartexpense.backend.services.TransactionService;
import com.smartexpense.backend.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private JwtUtils jwtUtils;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            return ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getTransactions() {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            List<Transaction> transactions = transactionService.findByUserId(userId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch transactions: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransaction(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Transaction> transaction = transactionService.findById(id);
            if (transaction.isPresent()) {
                // Check if transaction belongs to current user
                if (!transaction.get().getUserId().equals(userId)) {
                    return ResponseEntity.status(403).body("Access denied");
                }
                return ResponseEntity.ok(transaction.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch transaction: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody Transaction transaction) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            transaction.setUserId(userId);
            Transaction savedTransaction = transactionService.save(transaction);
            return ResponseEntity.ok(savedTransaction);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create transaction: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable String id, @RequestBody Transaction transaction) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Transaction> existingTransaction = transactionService.findById(id);
            if (existingTransaction.isPresent()) {
                if (!existingTransaction.get().getUserId().equals(userId)) {
                    return ResponseEntity.status(403).body("Access denied");
                }

                transaction.setId(id);
                transaction.setUserId(userId);
                Transaction updatedTransaction = transactionService.save(transaction);
                return ResponseEntity.ok(updatedTransaction);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update transaction: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Transaction> transaction = transactionService.findById(id);
            if (transaction.isPresent()) {
                if (!transaction.get().getUserId().equals(userId)) {
                    return ResponseEntity.status(403).body("Access denied");
                }

                transactionService.deleteById(id);
                return ResponseEntity.ok("Transaction deleted successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete transaction: " + e.getMessage());
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentTransactions() {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            List<Transaction> transactions = transactionService.findByUserId(userId);
            // Sort by date descending
            transactions.sort((t1, t2) -> {
                if (t1.getTransactionDate() == null || t2.getTransactionDate() == null) return 0;
                return t2.getTransactionDate().compareTo(t1.getTransactionDate());
            });
            // Return top 5
            return ResponseEntity.ok(transactions.stream().limit(5).toList());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch recent transactions: " + e.getMessage());
        }
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getTransactionSummary() {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            List<Transaction> transactions = transactionService.findByUserId(userId);
            java.util.Map<String, Double> summary = new java.util.HashMap<>();
            for (Transaction t : transactions) {
                if ("DEBIT".equalsIgnoreCase(t.getType()) || "expense".equalsIgnoreCase(t.getType())) {
                    String category = t.getCategory() != null ? t.getCategory() : "Other";
                    summary.put(category, summary.getOrDefault(category, 0.0) + t.getAmount());
                }
            }
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch transaction summary: " + e.getMessage());
        }
    }
}
