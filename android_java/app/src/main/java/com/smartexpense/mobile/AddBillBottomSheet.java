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
import com.smartexpense.mobile.network.*;
import retrofit2.*;
import java.util.Map;
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

    private Runnable onDismissListener;

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
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
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
            String uid = prefs.getString("userId", null);
            
            if (uid != null) {
                com.smartexpense.mobile.model.Bill bill = new com.smartexpense.mobile.model.Bill();
                bill.userId = uid;
                bill.billerName = name;
                bill.amountDue = Double.parseDouble(amount);
                bill.dueDate = dueDay;
                bill.billType = category.isEmpty() ? "Other" : category;
                bill.isPaid = false;
                
                btnSave.setEnabled(false);
                btnSave.setText("Saving...");
                
                com.smartexpense.mobile.network.RetrofitClient.getClient().create(com.smartexpense.mobile.network.ApiService.class).createBill(bill)
                    .enqueue(new retrofit2.Callback<com.smartexpense.mobile.model.Bill>() {
                        @Override
                        public void onResponse(retrofit2.Call<com.smartexpense.mobile.model.Bill> call, retrofit2.Response<com.smartexpense.mobile.model.Bill> response) {
                            btnSave.setEnabled(true);
                            btnSave.setText("Save Bill");
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Bill added successfully", Toast.LENGTH_SHORT).show();
                                if (onDismissListener != null) onDismissListener.run();
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), "Failed to save bill", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<com.smartexpense.mobile.model.Bill> call, Throwable t) {
                            btnSave.setEnabled(true);
                            btnSave.setText("Save Bill");
                            Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                        }
                    });
            } else {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid data", Toast.LENGTH_SHORT).show();
        }
    }
}
