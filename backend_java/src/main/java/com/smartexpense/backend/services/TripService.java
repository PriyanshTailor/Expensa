package com.smartexpense.backend.services;

import com.smartexpense.backend.models.Trip;
import com.smartexpense.backend.repositories.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    /**
     * Create a new trip with validation
     */
    public Trip createTrip(Trip trip) {
        if (trip == null) {
            throw new IllegalArgumentException("Trip object cannot be null");
        }
        if (trip.getName() == null || trip.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Notebook name cannot be empty");
        }
        if (trip.getCreatedBy() == null || trip.getCreatedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID (createdBy) is required");
        }

        // Initialize collections
        if (trip.getMembers() == null) {
            trip.setMembers(new HashMap<>());
        } else {
            // Remove null or empty member names
            trip.getMembers().keySet().removeIf(key -> key == null || key.trim().isEmpty());
        }
        
        // At least one member is good practice but not strictly required by DB
        if (trip.getMembers().isEmpty()) {
            trip.getMembers().put("Self", 0.0);
        }
        
        if (trip.getExpenses() == null) {
            trip.setExpenses(new java.util.ArrayList<>());
        }

        trip.setCreatedAt(LocalDateTime.now());
        trip.setUpdatedAt(LocalDateTime.now());

        System.out.println("Creating Notebook: '" + trip.getName() + "' for User: " + trip.getCreatedBy());
        
        try {
            Trip saved = tripRepository.save(trip);
            System.out.println("Successfully saved Notebook with ID: " + saved.getId());
            return saved;
        } catch (Exception e) {
            System.err.println("Database Error saving Notebook: " + e.getMessage());
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    /**
     * Get all trips for a user
     */
    public List<Trip> getTripsByUserId(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        return tripRepository.findByCreatedBy(userId);
    }

    /**
     * Get a specific trip by ID
     */
    public Optional<Trip> getTripById(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID cannot be empty");
        }
        return tripRepository.findById(id);
    }

    /**
     * Update an existing trip
     */
    public Trip updateTrip(Trip trip) {
        if (trip == null) {
            throw new IllegalArgumentException("Trip object cannot be null");
        }
        if (trip.getId() == null) {
            throw new IllegalArgumentException("Trip ID is required for update");
        }
        
        // Verify trip exists
        if (!tripRepository.existsById(trip.getId())) {
            throw new IllegalArgumentException("Trip not found with ID: " + trip.getId());
        }
        
        trip.setUpdatedAt(LocalDateTime.now());
        return tripRepository.save(trip);
    }

    /**
     * Delete a trip
     */
    public void deleteTrip(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID cannot be empty");
        }
        if (!tripRepository.existsById(id)) {
            throw new IllegalArgumentException("Trip not found with ID: " + id);
        }
        tripRepository.deleteById(id);
    }

    /**
     * Add an expense to a trip for a specific member
     */
    public Trip addExpenseToTrip(String tripId, String memberName, Double amount, String description) {
        if (tripId == null || memberName == null || amount == null) {
            throw new IllegalArgumentException("Trip ID, member name, and amount are required");
        }
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (amount == 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }

        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (!tripOpt.isPresent()) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        Trip trip = tripOpt.get();
        
        // Validate member exists
        if (!trip.getMembers().containsKey(memberName)) {
            throw new IllegalArgumentException("Member '" + memberName + "' not found in trip");
        }

        // Ensure collections exist
        if (trip.getExpenses() == null) {
            trip.setExpenses(new java.util.ArrayList<>());
        }

        // Add/update member total
        double currentTotal = trip.getMembers().getOrDefault(memberName, 0.0);
        trip.getMembers().put(memberName, currentTotal + amount);

        // Add expense to log
        Trip.TripExpense expense = new Trip.TripExpense(
            UUID.randomUUID().toString(),
            memberName,
            amount,
            description != null ? description : "",
            LocalDateTime.now(),
            "General"
        );
        trip.getExpenses().add(expense);
        trip.setUpdatedAt(LocalDateTime.now());

        return tripRepository.save(trip);
    }

    /**
     * Add a new member to the trip
     */
    public Trip addMemberToTrip(String tripId, String memberName) {
        if (tripId == null || memberName == null || memberName.trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID and member name are required");
        }

        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (!tripOpt.isPresent()) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        Trip trip = tripOpt.get();
        
        // Ensure members map exists
        if (trip.getMembers() == null) {
            trip.setMembers(new HashMap<>());
        }
        
        // Check if member already exists
        if (trip.getMembers().containsKey(memberName)) {
            throw new IllegalArgumentException("Member '" + memberName + "' already exists in this trip");
        }
        
        trip.getMembers().put(memberName, 0.0);
        trip.setUpdatedAt(LocalDateTime.now());
        return tripRepository.save(trip);
    }

    /**
     * Get all expenses for a trip
     */
    public List<Trip.TripExpense> getTripExpenses(String tripId) {
        if (tripId == null || tripId.trim().isEmpty()) {
            throw new IllegalArgumentException("Trip ID cannot be empty");
        }

        Optional<Trip> tripOpt = tripRepository.findById(tripId);
        if (!tripOpt.isPresent()) {
            throw new IllegalArgumentException("Trip not found with ID: " + tripId);
        }

        Trip trip = tripOpt.get();
        return trip.getExpenses() != null ? trip.getExpenses() : new java.util.ArrayList<>();
    }
}
