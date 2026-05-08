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

public class AddBillBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText etName, etAmount, etDueDay;
    private AutoCompleteTextView spinnerCategory;
    private Button btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_bill_bottom_sheet, container, false);

        etName = view.findViewById(R.id.etBillName);
        etAmount = view.findViewById(R.id.etBillAmount);
        etDueDay = view.findViewById(R.id.etDueDay);
        spinnerCategory = view.findViewById(R.id.spinnerBillCategory);
        btnSave = view.findViewById(R.id.btnSaveBill);

        String[] categories = {"Electricity", "Water", "Gas", "Internet", "Mobile", "Rent", "Insurance", "Other"};
        spinnerCategory.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, categories));

        btnSave.setOnClickListener(v -> saveBill());

        return view;
    }

    private void saveBill() {
        String name = etName.getText().toString();
        String amount = etAmount.getText().toString();
        String dueDay = etDueDay.getText().toString();
        String category = spinnerCategory.getText().toString();

        if (name.isEmpty() || amount.isEmpty() || dueDay.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            com.smartexpense.mobile.model.Bill bill = new com.smartexpense.mobile.model.Bill();
            bill.billerName = name;
            bill.amountDue = Double.parseDouble(amount);
            bill.dueDate = dueDay; // Can also append "th" if needed
            bill.billType = category.isEmpty() ? "Other" : category;
            bill.isPaid = false;
            bill.rawSms = "Manual entry";
            bill.detectedAt = System.currentTimeMillis();
            bill.userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();

            new Thread(() -> {
                try {
                    com.smartexpense.mobile.database.AppDatabase.getDatabase(getContext()).billDao().insert(bill);
                    
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Bill saved", Toast.LENGTH_SHORT).show();
                            dismiss();
                        });
                    }
                } catch (Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "Error saving bill", Toast.LENGTH_SHORT).show();
                            dismiss();
                        });
                    }
                }
            }).start();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid data", Toast.LENGTH_SHORT).show();
        }
    }
}
