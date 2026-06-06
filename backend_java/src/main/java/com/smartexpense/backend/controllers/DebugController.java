package com.smartexpense.backend.controllers;

import com.smartexpense.backend.repositories.TripRepository;
import com.smartexpense.backend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
public class DebugController {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalTrips", tripRepository.count());
        stats.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("message", "Backend is running correctly and reachable");
        return ResponseEntity.ok(status);
    }
}
