package com.smartexpense.backend.services;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.smartexpense.backend.models.Budget;
import com.smartexpense.backend.models.Transaction;
import com.smartexpense.backend.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class FirestoreService {

    @Autowired
    private Firestore firestore;

    // --- User Operations ---

    public User saveUser(User user) throws ExecutionException, InterruptedException {
        if (user.getId() == null) {
            user.setId(firestore.collection("users").document().getId());
        }
        ApiFuture<WriteResult> future = firestore.collection("users").document(user.getEmail()).set(user);
        future.get();
        return user;
    }

    public User getUser(String email) throws ExecutionException, InterruptedException {
        DocumentReference documentReference = firestore.collection("users").document(email);
        ApiFuture<DocumentSnapshot> future = documentReference.get();
        DocumentSnapshot document = future.get();
        if (document.exists()) {
            return document.toObject(User.class);
        }
        return null;
    }

    // --- Transaction Operations ---

    public Transaction saveTransaction(Transaction transaction) throws ExecutionException, InterruptedException {
        if (transaction.getId() == null) {
            transaction.setId(firestore.collection("transactions").document().getId());
        }
        ApiFuture<WriteResult> future = firestore.collection("transactions").document(transaction.getId()).set(transaction);
        future.get();
        return transaction;
    }

    public List<Transaction> getTransactionsByUserId(String userId) throws ExecutionException, InterruptedException {
        CollectionReference transactions = firestore.collection("transactions");
        Query query = transactions.whereEqualTo("userId", userId).orderBy("transactionDate", Query.Direction.DESCENDING);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<Transaction> result = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            result.add(document.toObject(Transaction.class));
        }
        return result;
    }

    // --- Budget Operations ---

    public Budget saveBudget(Budget budget) throws ExecutionException, InterruptedException {
        if (budget.getId() == null) {
            budget.setId(firestore.collection("budgets").document().getId());
        }
        ApiFuture<WriteResult> future = firestore.collection("budgets").document(budget.getId()).set(budget);
        future.get();
        return budget;
    }

    public List<Budget> getBudgetsByUserId(String userId) throws ExecutionException, InterruptedException {
        CollectionReference budgets = firestore.collection("budgets");
        Query query = budgets.whereEqualTo("userId", userId);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<Budget> result = new ArrayList<>();
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            result.add(document.toObject(Budget.class));
        }
        return result;
    }
}
