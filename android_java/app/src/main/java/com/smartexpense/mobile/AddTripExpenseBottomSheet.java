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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import java.util.Map;

public class AddTripExpenseBottomSheet extends BottomSheetDialogFragment {

    private AutoCompleteTextView spinnerMember;
    private TextInputEditText etAmount, etDescription;
    private Button btnSave;
    private FirebaseFirestore db;
    private String tripId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_trip_expense, container, false);

        if (getArguments() != null) {
            tripId = getArguments().getString("TRIP_ID");
        }

        spinnerMember = view.findViewById(R.id.spinnerMember);
        etAmount = view.findViewById(R.id.etTripAmount);
        etDescription = view.findViewById(R.id.etTripDescription);
        btnSave = view.findViewById(R.id.btnSaveTripExpense);

        db = FirebaseFirestore.getInstance();

        if (tripId != null) {
            db.collection("trips").document(tripId).get().addOnSuccessListener(doc -> {
                if (doc != null && doc.exists()) {
                    Map<String, Object> membersMap = (Map<String, Object>) doc.get("members");
                    if (membersMap != null) {
                        String[] members = membersMap.keySet().toArray(new String[0]);
                        spinnerMember.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, members));
                    }
                }
            });
        }

        btnSave.setOnClickListener(v -> saveExpense());

        return view;
    }

    private void saveExpense() {
        String member = spinnerMember.getText().toString();
        String amountStr = etAmount.getText().toString();
        String desc = etDescription.getText().toString();

        if (member.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill member and amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            
            if (tripId == null) return;
            
            // Increment the specific member's total in Firestore using FieldValue.increment
            db.collection("trips").document(tripId)
                .update("members." + member, FieldValue.increment(amount))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Added " + member + "'s expense!", Toast.LENGTH_SHORT).show();
                    dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to sync to trip", Toast.LENGTH_SHORT).show();
                });
                
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }
}
