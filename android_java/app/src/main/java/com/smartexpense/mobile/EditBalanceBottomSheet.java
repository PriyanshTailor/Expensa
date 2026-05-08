package com.smartexpense.mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class EditBalanceBottomSheet extends BottomSheetDialogFragment {

    private double currentSavings;
    private double currentMF;
    private OnBalanceUpdatedListener listener;

    public interface OnBalanceUpdatedListener {
        void onUpdated();
    }

    public static EditBalanceBottomSheet newInstance(double currentSavings, double currentMF, OnBalanceUpdatedListener listener) {
        EditBalanceBottomSheet fragment = new EditBalanceBottomSheet();
        fragment.currentSavings = currentSavings;
        fragment.currentMF = currentMF;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_edit_balance, container, false);

        TextView tvTitle = view.findViewById(R.id.tvEditTitle);
        TextInputEditText etSavings = view.findViewById(R.id.etSavings);
        TextInputEditText etMF = view.findViewById(R.id.etMutualFunds);
        Button btnSave = view.findViewById(R.id.btnSaveBalance);

        tvTitle.setText("Update Wealth Details");
        etSavings.setText(String.valueOf(currentSavings));
        etMF.setText(String.valueOf(currentMF));

        btnSave.setOnClickListener(v -> {
            String valSavings = etSavings.getText().toString();
            String valMF = etMF.getText().toString();

            double newSavings = valSavings.isEmpty() ? 0 : Double.parseDouble(valSavings);
            double newMF = valMF.isEmpty() ? 0 : Double.parseDouble(valMF);

            try {
                String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                        .update("savingsBalance", newSavings, "mutualFunds", newMF)
                        .addOnSuccessListener(aVoid -> {
                            if (listener != null) listener.onUpdated();
                            dismiss();
                        });
            } catch (Exception e) {}
        });

        return view;
    }
}
