package com.smartexpense.backend.controllers;

import com.smartexpense.backend.models.Budget;
import com.smartexpense.backend.services.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    @Autowired
    private BudgetService budgetService;

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            return ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getBudgets() {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            List<Budget> budgets = budgetService.findByUserId(userId);
            return ResponseEntity.ok(budgets);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch budgets: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBudget(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Budget> budget = budgetService.findById(id);
            if (budget.isPresent()) {
                // Check if budget belongs to current user
                if (!budget.get().getUserId().equals(userId)) {
                    return ResponseEntity.status(403).body("Access denied");
                }
                return ResponseEntity.ok(budget.get());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to fetch budget: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> createBudget(@RequestBody Budget budget) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            budget.setUserId(userId);
            Budget savedBudget = budgetService.save(budget);
            return ResponseEntity.ok(savedBudget);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to create budget: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(@PathVariable String id, @RequestBody Budget budget) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Budget> existingBudget = budgetService.findById(id);
            if (existingBudget.isPresent()) {
                if (!existingBudget.get().getUserId().equals(userId)) {
                    return ResponseEntity.status(403).body("Access denied");
                }

                budget.setId(id);
                budget.setUserId(userId);
                Budget updatedBudget = budgetService.save(budget);
                return ResponseEntity.ok(updatedBudget);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to update budget: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable String id) {
        try {
            String userId = getCurrentUserId();
            if (userId == null) {
                return ResponseEntity.status(401).body("Unauthorized");
            }

            Optional<Budget> budget = budgetService.findById(id);
            if (budget.isPresent()) {
                if (!budget.get().getUserId().equals(userId)) {
                    return ResponseEntity.status(403).body("Access denied");
                }

                budgetService.deleteById(id);
                return ResponseEntity.ok("Budget deleted successfully");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed to delete budget: " + e.getMessage());
        }
    }
}
