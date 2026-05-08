package com.smartexpense.backend.services;

import com.smartexpense.backend.models.Budget;
import com.smartexpense.backend.repositories.BudgetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    public Budget save(Budget budget) {
        if (budget.getCreatedAt() == null) {
            budget.setCreatedAt(LocalDateTime.now());
        }
        budget.setUpdatedAt(LocalDateTime.now());
        return budgetRepository.save(budget);
    }

    public List<Budget> findByUserId(String userId) {
        return budgetRepository.findByUserId(userId);
    }

    public Optional<Budget> findById(String id) {
        return budgetRepository.findById(id);
    }

    public void deleteById(String id) {
        budgetRepository.deleteById(id);
    }
}
