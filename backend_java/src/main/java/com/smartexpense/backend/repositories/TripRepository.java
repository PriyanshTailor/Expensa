package com.smartexpense.backend.repositories;

import com.smartexpense.backend.models.Trip;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends MongoRepository<Trip, String> {
    List<Trip> findByCreatedBy(String createdBy);
    Optional<Trip> findById(String id);
    boolean existsById(String id);
    void deleteById(String id);
}
