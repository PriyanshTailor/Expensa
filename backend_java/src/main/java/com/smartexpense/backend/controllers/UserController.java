package com.smartexpense.backend.controllers;

import com.smartexpense.backend.models.User;
import com.smartexpense.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}/balance")
    public ResponseEntity<?> updateBalance(@PathVariable String id, @RequestBody Map<String, Double> payload) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (payload.containsKey("savingsBalance")) {
                user.setSavingsBalance(payload.get("savingsBalance"));
            }
            if (payload.containsKey("mutualFunds")) {
                user.setMutualFunds(payload.get("mutualFunds"));
            }
            userRepository.save(user);
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.notFound().build();
    }
}
