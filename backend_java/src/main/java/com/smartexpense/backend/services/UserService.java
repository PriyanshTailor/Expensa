package com.smartexpense.backend.services;

import com.smartexpense.backend.models.User;
import com.smartexpense.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User registerUser(User user) {
        try {
            // Check if user already exists
            if (userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new RuntimeException("Email already in use");
            }

            // Set timestamps
            user.setCreatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            // Hash password
            user.setPasswordHash(passwordEncoder.encode(user.getPassword()));

            User savedUser = userRepository.save(user);

            return savedUser;
        } catch (Exception e) {
            throw new RuntimeException("Error registering user: " + e.getMessage());
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User save(User user) {
        try {
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Error saving user: " + e.getMessage());
        }
    }

    public boolean validatePassword(User user, String rawPassword) {
        return passwordEncoder.matches(rawPassword, user.getPasswordHash());
    }
}
