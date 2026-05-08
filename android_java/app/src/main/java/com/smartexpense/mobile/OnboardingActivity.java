package com.smartexpense.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class OnboardingActivity extends AppCompatActivity {

    private ViewFlipper viewFlipper;
    private EditText etSavings, etMF, etBillName, etBillAmount, etBillDue;
    private Button btnNext1, btnNext2, btnNext3, btnFinish;
    private FirebaseFirestore db;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            finish();
            return;
        }

        viewFlipper = findViewById(R.id.viewFlipper);
        
        // Step 1
        etSavings = findViewById(R.id.etSavingsInitial);
        btnNext1 = findViewById(R.id.btnNext1);
        btnNext1.setOnClickListener(v -> viewFlipper.showNext());

        // Step 2
        etMF = findViewById(R.id.etMFInitial);
        btnNext2 = findViewById(R.id.btnNext2);
        btnNext2.setOnClickListener(v -> viewFlipper.showNext());

        // Step 3
        etBillName = findViewById(R.id.etBillNameInitial);
        etBillAmount = findViewById(R.id.etBillAmountInitial);
        etBillDue = findViewById(R.id.etBillDueInitial);
        btnNext3 = findViewById(R.id.btnNext3);
        btnNext3.setOnClickListener(v -> viewFlipper.showNext());

        // Step 4
        btnFinish = findViewById(R.id.btnFinishOnboarding);
        btnFinish.setOnClickListener(v -> saveAndFinish());
    }

    private void saveAndFinish() {
        Map<String, Object> data = new HashMap<>();
        String s = etSavings.getText().toString();
        String m = etMF.getText().toString();
        
        data.put("savingsBalance", Double.parseDouble(s.isEmpty() ? "0" : s));
        data.put("mutualFunds", Double.parseDouble(m.isEmpty() ? "0" : m));
        data.put("onboardingCompleted", true);

        db.collection("users").document(uid).set(data, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(OnboardingActivity.this, "Profile Setup Complete!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OnboardingActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(OnboardingActivity.this, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
