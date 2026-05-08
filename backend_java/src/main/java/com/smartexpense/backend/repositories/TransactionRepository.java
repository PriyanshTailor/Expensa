package com.smartexpense.backend.repositories;

import com.smartexpense.backend.models.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUserId(String userId);
    Optional<Transaction> findByIdAndUserId(String id, String userId);
}

