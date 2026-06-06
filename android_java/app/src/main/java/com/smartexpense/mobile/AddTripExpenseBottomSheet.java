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


import com.smartexpense.mobile.model.Trip;

import java.util.HashMap;
import java.util.Map;



public class AddTripExpenseBottomSheet extends BottomSheetDialogFragment {

    private AutoCompleteTextView spinnerMember;
    private TextInputEditText etAmount, etDescription;
    private Button btnSave;
    
    private String tripId;
    private Runnable onDismissListener;

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
    }

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

        

        if (tripId != null) {
            // Fetch trip details to load members
            com.smartexpense.mobile.network.RetrofitClient.getClient().create(com.smartexpense.mobile.network.ApiService.class).getTrip(tripId)
                .enqueue(new retrofit2.Callback<Trip>() {
                    @Override
                    public void onResponse(retrofit2.Call<Trip> call, retrofit2.Response<Trip> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Trip trip = response.body();
                            Map<String, Double> membersMap = trip.getMembers();
                            if (membersMap != null && !membersMap.isEmpty()) {
                                String[] members = membersMap.keySet().toArray(new String[0]);
                                spinnerMember.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, members));
                                if (members.length > 0) {
                                    spinnerMember.setText(members[0], false);
                                }
                            } else {
                                Toast.makeText(getContext(), "No members", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    @Override
                    public void onFailure(retrofit2.Call<Trip> call, Throwable t) {}
                });
        } else {
            Toast.makeText(getContext(), "Invalid notebook ID", Toast.LENGTH_SHORT).show();
        }

        btnSave.setOnClickListener(v -> saveExpense());

        return view;
    }

    private void saveExpense() {
        String member = spinnerMember.getText().toString().trim();
        String amountStr = etAmount.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        if (member.isEmpty()) {
            Toast.makeText(getContext(), "Please select a member", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amountStr.isEmpty()) {
            Toast.makeText(getContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                Toast.makeText(getContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (tripId == null || tripId.isEmpty()) {
                Toast.makeText(getContext(), "Invalid notebook ID", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("memberName", member);
            data.put("amount", amount);
            if (!description.isEmpty()) {
                data.put("description", description);
            }

            btnSave.setEnabled(false);
            btnSave.setText("Saving...");

            com.smartexpense.mobile.network.RetrofitClient.getClient().create(com.smartexpense.mobile.network.ApiService.class).addTripExpense(tripId, data)
                .enqueue(new retrofit2.Callback<Trip>() {
                    @Override
                    public void onResponse(retrofit2.Call<Trip> call, retrofit2.Response<Trip> response) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save Expense");
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "Expense added", Toast.LENGTH_SHORT).show();
                            if (onDismissListener != null) onDismissListener.run();
                            dismiss();
                        } else {
                            Toast.makeText(getContext(), "Failed to add expense", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(retrofit2.Call<Trip> call, Throwable t) {
                        btnSave.setEnabled(true);
                        btnSave.setText("Save Expense");
                        Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                    }
                });
                
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
