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
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddTripBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText etName, etMembers;
    private Button btnCreate;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_add_new_trip, container, false);

        etName = view.findViewById(R.id.etTripName);
        etMembers = view.findViewById(R.id.etTripMembers);
        btnCreate = view.findViewById(R.id.btnCreateTrip);
        db = FirebaseFirestore.getInstance();

        btnCreate.setOnClickListener(v -> createTrip());

        return view;
    }

    private void createTrip() {
        String name = etName.getText().toString().trim();
        String membersStr = etMembers.getText().toString().trim();

        if (name.isEmpty() || membersStr.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> tripData = new HashMap<>();
        tripData.put("name", name);
        
        Map<String, Double> membersMap = new HashMap<>();
        String[] membersArray = membersStr.split(",");
        for (String m : membersArray) {
            String cleanName = m.trim();
            if (!cleanName.isEmpty()) {
                membersMap.put(cleanName, 0.0);
            }
        }
        
        tripData.put("members", membersMap);
        
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            tripData.put("createdBy", uid);
        }

        db.collection("trips").add(tripData)
            .addOnSuccessListener(docRef -> {
                Toast.makeText(getContext(), "Notebook Created!", Toast.LENGTH_SHORT).show();
                dismiss();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to create", Toast.LENGTH_SHORT).show();
            });
    }
}
