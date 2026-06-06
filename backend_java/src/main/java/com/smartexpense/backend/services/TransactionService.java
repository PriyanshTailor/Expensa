package com.smartexpense.backend.services;

import com.smartexpense.backend.models.Transaction;
import com.smartexpense.backend.repositories.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction save(Transaction transaction) {
        if (transaction.getCreatedAt() == null) {
            transaction.setCreatedAt(LocalDateTime.now());
        }
        if (transaction.getDate() == null || transaction.getDate() == 0L) {
            transaction.setDate(System.currentTimeMillis());
        }
        transaction.setUpdatedAt(LocalDateTime.now());
        return transactionRepository.save(transaction);
    }

    public List<Transaction> findByUserId(String userId) {
        return transactionRepository.findByUserId(userId);
    }

    public Optional<Transaction> findById(String id) {
        return transactionRepository.findById(id);
    }

    public void deleteById(String id) {
        transactionRepository.deleteById(id);
    }
}
