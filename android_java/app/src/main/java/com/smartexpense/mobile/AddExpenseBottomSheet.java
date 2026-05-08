package com.smartexpense.mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddExpenseBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText etAmount, etNote;
    private AutoCompleteTextView spinnerCategory, spinnerMode, spinnerType;
    private Button btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_expense_bottom_sheet, container, false);

        etAmount = view.findViewById(R.id.etAmount);
        etNote = view.findViewById(R.id.etNote);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerMode = view.findViewById(R.id.spinnerMode);
        spinnerType = view.findViewById(R.id.spinnerType);
        btnSave = view.findViewById(R.id.btnSaveExpense);

        setupSpinners();

        btnSave.setOnClickListener(v -> saveExpense());

        return view;
    }

    private void setupSpinners() {
        String[] categories = {"Food & Dining", "Transport", "Shopping", "Entertainment", "Bills & Utilities", "Health", "Education", "Groceries", "Travel", "Other"};
        String[] modes = {"UPI", "Cash", "Credit Card", "Debit Card", "Net Banking"};
        String[] types = {"Debit", "Credit"};

        spinnerCategory.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categories));
        spinnerMode.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, modes));
        spinnerType.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, types));
    }

    private void saveExpense() {
        String amount = etAmount.getText().toString();
        String category = spinnerCategory.getText().toString();
        String mode = spinnerMode.getText().toString();
        String type = spinnerType.getText().toString();
        String note = etNote.getText().toString();

        if (amount.isEmpty() || category.isEmpty()) {
            Toast.makeText(getContext(), "Please fill amount and category", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amountVal = Double.parseDouble(amount);
            
            com.smartexpense.mobile.model.Transaction t = new com.smartexpense.mobile.model.Transaction();
            t.amount = amountVal;
            t.category = category;
            t.type = type.toUpperCase();
            t.merchant = note.isEmpty() ? mode : note; // Use note as merchant or fallback to mode
            t.source = "MANUAL";
            t.pendingReview = false;
            t.timestamp = System.currentTimeMillis();
            t.rawSms = "";
            t.accountLast4 = "";
            t.userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
            
            new Thread(() -> {
                try {
                    com.smartexpense.mobile.database.AppDatabase.getDatabase(getContext()).transactionDao().insert(t);
                    
                    // Update Firebase balance for sync
                    String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null ? 
                                 com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
                    if (uid != null) {
                        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
                        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
                            Double bal = userDoc.getDouble("savingsBalance");
                            if (bal == null) bal = 0.0;
                            if ("DEBIT".equals(t.type)) bal -= t.amount;
                            else bal += t.amount;
                            db.collection("users").document(uid).update("savingsBalance", bal);
                        });
                    }
                    
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Expense saved", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                } catch (Exception e) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Failed to save", Toast.LENGTH_SHORT).show();
                        dismiss();
                    });
                }
            }).start();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }
}
