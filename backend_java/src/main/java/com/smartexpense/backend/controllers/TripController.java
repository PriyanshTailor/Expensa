package com.smartexpense.backend.controllers;

import com.smartexpense.backend.models.Trip;
import com.smartexpense.backend.services.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "*")
public class TripController {

    @Autowired
    private TripService tripService;

    @PostMapping
    public ResponseEntity<?> createTrip(@RequestBody Trip trip) {
        if (trip != null) {
            System.out.println(">>> REQUEST: Create Notebook '" + trip.getName() + "' by User: " + trip.getCreatedBy());
        }
        try {
            if (trip == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Request body is empty"));
            }
            if (trip.getCreatedBy() == null || trip.getCreatedBy().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("User ID (createdBy) is missing in request"));
            }
            Trip created = tripService.createTrip(trip);
            System.out.println("<<< SUCCESS: Notebook created with ID: " + created.getId());
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            System.err.println("!!! ERROR creating notebook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Backend Error: " + e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getTripsByUser(@PathVariable String userId) {
        try {
            List<Trip> trips = tripService.getTripsByUserId(userId);
            return ResponseEntity.ok(trips);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch trips: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTrip(@PathVariable String id) {
        try {
            return tripService.getTripById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to fetch trip: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/member-expense")
    public ResponseEntity<?> addMemberExpense(@PathVariable String id, @RequestBody Map<String, Object> request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Request body cannot be null"));
            }

            String memberName = (String) request.get("memberName");
            Object amountObj = request.get("amount");
            String description = (String) request.getOrDefault("description", "");

            if (memberName == null || memberName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Member name is required"));
            }

            if (amountObj == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Amount is required"));
            }

            Double amount;
            try {
                amount = Double.parseDouble(amountObj.toString());
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Amount must be a valid number"));
            }

            if (amount == null || amount < 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Amount must be non-negative"));
            }

            if (amount == 0) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Amount must be greater than 0"));
            }

            Trip trip = tripService.addExpenseToTrip(id, memberName, amount, description);
            return ResponseEntity.ok(trip);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to add expense: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<?> addMember(@PathVariable String id, @RequestBody Map<String, String> request) {
        try {
            if (id == null || id.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Notebook ID is required"));
            }
            if (request == null) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Request body cannot be null"));
            }

            String name = request.get("name");
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Member name is required"));
            }

            if (name.length() > 100) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Member name too long (max 100 characters)"));
            }

            Trip trip = tripService.addMemberToTrip(id, name);
            return ResponseEntity.ok(trip);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to add member: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTrip(@PathVariable String id) {
        try {
            tripService.deleteTrip(id);
            return ResponseEntity.ok(new SuccessResponse("Trip deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to delete trip: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTrip(@PathVariable String id, @RequestBody Trip trip) {
        try {
            trip.setId(id);
            Trip updated = tripService.updateTrip(trip);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Failed to update trip: " + e.getMessage()));
        }
    }

    // Inner classes for responses
    public static class ErrorResponse {
        public String error;
        public long timestamp;

        public ErrorResponse(String error) {
            this.error = error;
            this.timestamp = System.currentTimeMillis();
        }
    }

    public static class SuccessResponse {
        public String message;
        public long timestamp;

        public SuccessResponse(String message) {
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
