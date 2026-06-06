package com.smartexpense.mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

import com.smartexpense.mobile.model.Trip;

import java.util.HashMap;
import java.util.Map;

public class AddTripBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText etName, etMembers;
    private Button btnCreate;
    
    private Runnable onDismissListener;

    public void setOnDismissListener(Runnable listener) {
        this.onDismissListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_new_trip, container, false);

        etName = view.findViewById(R.id.etTripName);
        etMembers = view.findViewById(R.id.etTripMembers);
        btnCreate = view.findViewById(R.id.btnCreateTrip);
        
        

        btnCreate.setOnClickListener(v -> createTrip());

        return view;
    }

    private void createTrip() {
        String name = etName.getText().toString().trim();
        String membersStr = etMembers.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter notebook name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (membersStr.isEmpty()) {
            Toast.makeText(getContext(), "Please add at least one member (comma-separated)", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> tripData = new HashMap<>();
        tripData.put("name", name);
        
        Map<String, Double> membersMap = new HashMap<>();
        String[] membersArray = membersStr.split(",");

        if (membersArray.length == 0) {
            Toast.makeText(getContext(), "Please add valid members", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String m : membersArray) {
            String cleanName = m.trim();
            if (!cleanName.isEmpty()) {
                membersMap.put(cleanName, 0.0);
            }
        }
        
        if (membersMap.isEmpty()) {
            Toast.makeText(getContext(), "Please add valid members", Toast.LENGTH_SHORT).show();
            return;
        }

        tripData.put("members", membersMap);
        
        android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
        String uid = prefs.getString("userId", null);
        if (uid == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        tripData.put("createdBy", uid);

        btnCreate.setEnabled(false);
        btnCreate.setText("Creating...");

        com.smartexpense.mobile.network.RetrofitClient.getClient().create(com.smartexpense.mobile.network.ApiService.class).createTrip(tripData)
            .enqueue(new retrofit2.Callback<Trip>() {
                @Override
                public void onResponse(retrofit2.Call<Trip> call, retrofit2.Response<Trip> response) {
                    btnCreate.setEnabled(true);
                    btnCreate.setText("Create Notebook");
                    if (response.isSuccessful()) {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Notebook Created!", Toast.LENGTH_SHORT).show();
                        }
                        if (onDismissListener != null) onDismissListener.run();
                        dismiss();
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Failed to create notebook", Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<Trip> call, Throwable t) {
                    btnCreate.setEnabled(true);
                    btnCreate.setText("Create Notebook");
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}
