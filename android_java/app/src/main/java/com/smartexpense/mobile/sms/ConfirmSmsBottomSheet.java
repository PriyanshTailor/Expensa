package com.smartexpense.mobile.sms;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.firestore.FirebaseFirestore;
import com.smartexpense.mobile.R;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ConfirmSmsBottomSheet extends BottomSheetDialogFragment {

    private TextInputEditText etSmsRaw;
    private Button btnDetect, btnConfirm;
    private LinearLayout layoutResult;
    private SmsParser.ParsedData currentParsedData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_confirm_sms, container, false);

        etSmsRaw = view.findViewById(R.id.etSmsRaw);
        btnDetect = view.findViewById(R.id.btnDetect);
        btnConfirm = view.findViewById(R.id.btnConfirmSms);
        layoutResult = view.findViewById(R.id.layoutParsedResult);

        btnDetect.setOnClickListener(v -> {
            String text = etSmsRaw.getText().toString();
            if (!text.isEmpty()) {
                currentParsedData = SmsParser.parseMessage(text, System.currentTimeMillis(), text);
                if (currentParsedData != null) {
                    updateResultUI(view);
                    layoutResult.setVisibility(View.VISIBLE);
                } else {
                    android.widget.Toast.makeText(getContext(), "Could not parse SMS. Try another format.", android.widget.Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnConfirm.setOnClickListener(v -> saveTransaction());

        return view;
    }

    private void updateResultUI(View view) {
        TextView tvMerchant = view.findViewById(R.id.tvMerchant);
        TextView tvCategory = view.findViewById(R.id.tvCategory);
        TextView tvAmount = view.findViewById(R.id.tvAmount);

        if (currentParsedData.isBill()) {
            tvMerchant.setText(currentParsedData.bill.billerName);
            tvCategory.setText(currentParsedData.bill.billType);
            tvAmount.setText(NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(currentParsedData.bill.amountDue));
        } else {
            tvMerchant.setText(currentParsedData.transaction.merchant);
            tvCategory.setText(currentParsedData.transaction.category);
            tvAmount.setText(NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(currentParsedData.transaction.amount));
        }
    }

    private void saveTransaction() {
        if (currentParsedData == null) return;

        new Thread(() -> {
            try {
                com.smartexpense.mobile.database.AppDatabase db = com.smartexpense.mobile.database.AppDatabase.getDatabase(getContext());
                android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
                String uid = prefs.getString("userId", null);

                if (currentParsedData.isBill()) {
                    currentParsedData.bill.userId = uid;
                    db.billDao().insert(currentParsedData.bill);
                } else {
                    currentParsedData.transaction.userId = uid;
                    db.transactionDao().insert(currentParsedData.transaction);
                }
                
                // Removed Firebase code
                
                getActivity().runOnUiThread(() -> dismiss());
            } catch (Exception e) {
                getActivity().runOnUiThread(() -> dismiss());
            }
        }).start();
    }
}
