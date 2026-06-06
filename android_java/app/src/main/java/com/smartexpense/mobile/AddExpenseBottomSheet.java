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

    private Runnable onDismissListener;

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
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
            
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
            String uid = prefs.getString("userId", null);
            
            if (uid != null) {
                String merchant = note.isEmpty() ? mode : note;
                String finalType = type.toUpperCase();
                
                Map<String, Object> data = new HashMap<>();
                data.put("amount", amountVal);
                data.put("merchant", merchant);
                data.put("category", category);
                data.put("type", finalType);
                data.put("date", new java.util.Date().getTime()); // send as epoch ms
                data.put("source", "MANUAL");
                data.put("rawMessage", "");

                com.smartexpense.mobile.network.RetrofitClient.getClient().create(com.smartexpense.mobile.network.ApiService.class).createTransaction(data)
                    .enqueue(new retrofit2.Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(retrofit2.Call<Map<String, Object>> call, retrofit2.Response<Map<String, Object>> response) {
                            if (response.isSuccessful()) {
                                // Update balance logic
                                com.smartexpense.mobile.network.ApiService api = com.smartexpense.mobile.network.RetrofitClient.getClient().create(com.smartexpense.mobile.network.ApiService.class);
                                api.getUserProfile(uid).enqueue(new retrofit2.Callback<Map<String, Object>>() {
                                    @Override
                                    public void onResponse(retrofit2.Call<Map<String, Object>> c, retrofit2.Response<Map<String, Object>> r) {
                                        if (r.isSuccessful() && r.body() != null) {
                                            Double bal = r.body().get("savingsBalance") instanceof Number ? ((Number) r.body().get("savingsBalance")).doubleValue() : 0.0;
                                            if ("DEBIT".equals(finalType)) bal -= amountVal;
                                            else bal += amountVal;
                                            
                                            Map<String, Double> bData = new HashMap<>();
                                            bData.put("savingsBalance", bal);
                                            api.updateBalance(uid, bData).enqueue(new retrofit2.Callback<Map<String, Object>>() {
                                                public void onResponse(retrofit2.Call<Map<String, Object>> cc, retrofit2.Response<Map<String, Object>> rr) {}
                                                public void onFailure(retrofit2.Call<Map<String, Object>> cc, Throwable tt) {}
                                            });
                                        }
                                    }
                                    public void onFailure(retrofit2.Call<Map<String, Object>> c, Throwable t) {}
                                });
                                
                                Toast.makeText(getContext(), "Expense saved", Toast.LENGTH_SHORT).show();
                                if (onDismissListener != null) {
                                    onDismissListener.run();
                                }
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), "Failed to save expense", Toast.LENGTH_SHORT).show();
                            }
                        }
                        @Override
                        public void onFailure(retrofit2.Call<Map<String, Object>> call, Throwable t) {
                            Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
                        }
                    });
            } else {
                Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                dismiss();
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
        }
    }
}
