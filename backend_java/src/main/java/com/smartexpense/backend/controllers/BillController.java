package com.smartexpense.backend.controllers;

import com.smartexpense.backend.models.Bill;
import com.smartexpense.backend.services.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@CrossOrigin(origins = "*")
public class BillController {

    @Autowired
    private BillService billService;

    @PostMapping
    public ResponseEntity<?> createBill(@RequestBody Bill bill) {
        try {
            if (bill.getUserId() == null || bill.getUserId().isEmpty()) {
                return ResponseEntity.badRequest().body("User ID is required");
            }
            Bill created = billService.createBill(bill);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Backend Error: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getBillsByUser(@PathVariable String userId) {
        try {
            List<Bill> bills = billService.getBillsByUserId(userId);
            return ResponseEntity.ok(bills);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch bills: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<?> markBillAsPaid(@PathVariable String id) {
        try {
            Bill updated = billService.markAsPaid(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update bill: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBill(@PathVariable String id) {
        try {
            billService.deleteBill(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete bill: " + e.getMessage());
        }
    }
}
