package com.smartexpense.backend.services;

import com.smartexpense.backend.models.Bill;
import com.smartexpense.backend.repositories.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BillService {

    @Autowired
    private BillRepository billRepository;

    public Bill createBill(Bill bill) {
        if (bill.getDetectedAt() == 0) {
            bill.setDetectedAt(System.currentTimeMillis());
        }
        return billRepository.save(bill);
    }

    public List<Bill> getBillsByUserId(String userId) {
        return billRepository.findByUserId(userId);
    }

    public Optional<Bill> getBillById(String id) {
        return billRepository.findById(id);
    }

    public Bill updateBill(Bill bill) {
        return billRepository.save(bill);
    }

    public void deleteBill(String id) {
        billRepository.deleteById(id);
    }

    public Bill markAsPaid(String id) {
        Optional<Bill> optionalBill = billRepository.findById(id);
        if (optionalBill.isPresent()) {
            Bill bill = optionalBill.get();
            bill.setPaid(true);
            return billRepository.save(bill);
        }
        throw new IllegalArgumentException("Bill not found with ID: " + id);
    }
}
