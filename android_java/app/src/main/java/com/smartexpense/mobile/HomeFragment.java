package com.smartexpense.mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private TextView tvGreeting, tvUserName, tvTotalWealth, tvSavingsBalance, tvMFBalance;
    private ImageButton btnRefresh;
    private android.widget.Button btnAddUpdateWealth;
    private RecyclerView rvRecentSms;
    private LinearLayout layoutEmptySms;
    private FirebaseFirestore db;
    private String uid;
    private com.github.mikephil.charting.charts.PieChart pieChartHome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setGreeting();

        try {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                uid = user.getUid();
                db = FirebaseFirestore.getInstance();
                
                fetchUserData();
                fetchRecentSms();
                fetchChartData();
            } else {
                tvUserName.setText("Please Login");
            }
        } catch (Exception e) {
            tvUserName.setText("Config Error");
            Toast.makeText(getContext(), "Database not connected", Toast.LENGTH_LONG).show();
        }

        if (btnRefresh != null) btnRefresh.setOnClickListener(v -> {
            fetchUserData();
            fetchChartData();
        });
        
        if (btnAddUpdateWealth != null) {
            btnAddUpdateWealth.setOnClickListener(v -> showEditBalanceSheet());
        }
        
        View btnDetectSms = view.findViewById(R.id.btnDetectSms);
        if (btnDetectSms != null) {
            btnDetectSms.setOnClickListener(v -> {
                new com.smartexpense.mobile.sms.ConfirmSmsBottomSheet().show(getChildFragmentManager(), "sms_detect");
            });
        }

        View fabAdd = view.findViewById(R.id.fabAddExpenseHome);
        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                new AddExpenseBottomSheet().show(getParentFragmentManager(), "add_expense");
            });
        }

        return view;
    }

    private void initViews(View view) {
        tvGreeting = view.findViewById(R.id.tvGreeting);
        tvUserName = view.findViewById(R.id.tvUserName);
        tvTotalWealth = view.findViewById(R.id.tvTotalWealth);
        tvSavingsBalance = view.findViewById(R.id.tvSavingsBalance);
        tvMFBalance = view.findViewById(R.id.tvMFBalance);
        btnRefresh = view.findViewById(R.id.btnRefresh);
        btnAddUpdateWealth = view.findViewById(R.id.btnAddUpdateWealth);
        rvRecentSms = view.findViewById(R.id.rvRecentSms);
        layoutEmptySms = view.findViewById(R.id.layoutEmptySms);
        pieChartHome = view.findViewById(R.id.pieChartHome);

        if (pieChartHome != null) {
            pieChartHome.getDescription().setEnabled(false);
            pieChartHome.setHoleRadius(40f);
            pieChartHome.setTransparentCircleRadius(45f);
            pieChartHome.setDrawEntryLabels(false);
        }

        if (rvRecentSms != null) rvRecentSms.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setGreeting() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String greeting = "Good Morning,";

        if (timeOfDay >= 12 && timeOfDay < 16) {
            greeting = "Good Afternoon,";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            greeting = "Good Evening,";
        } else if (timeOfDay >= 21 || timeOfDay < 5) {
            greeting = "Good Night,";
        }
        tvGreeting.setText(greeting);
    }

    private void fetchUserData() {
        if (db == null || uid == null) return;

        com.google.firebase.auth.FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getDisplayName() != null) {
            tvUserName.setText(user.getDisplayName());
        }
        
        db.collection("users").document(uid).addSnapshotListener((documentSnapshot, error) -> {
            if (documentSnapshot != null && documentSnapshot.exists()) {
                String name = documentSnapshot.getString("name");
                Double savings = documentSnapshot.getDouble("savingsBalance");
                Double mf = documentSnapshot.getDouble("mutualFunds");

                if (name != null) tvUserName.setText(name);
                
                double sVal = savings != null ? savings : 0;
                double mVal = mf != null ? mf : 0;
                
                tvSavingsBalance.setText(formatCurrency(sVal));
                tvMFBalance.setText(formatCurrency(mVal));
                tvTotalWealth.setText(formatCurrency(sVal + mVal));

                if (btnAddUpdateWealth != null) {
                    if (sVal > 0 || mVal > 0 || savings != null || mf != null) {
                        btnAddUpdateWealth.setText("Update");
                    } else {
                        btnAddUpdateWealth.setText("Add");
                    }
                }
            }
        });
    }

    private void fetchRecentSms() {
        if (uid == null) return;
        
        com.smartexpense.mobile.database.AppDatabase roomDb = com.smartexpense.mobile.database.AppDatabase.getDatabase(getContext());
        roomDb.transactionDao().getAllTransactions(uid).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null || transactions.isEmpty()) {
                if (layoutEmptySms != null) layoutEmptySms.setVisibility(View.VISIBLE);
                if (rvRecentSms != null) rvRecentSms.setVisibility(View.GONE);
            } else {
                if (layoutEmptySms != null) layoutEmptySms.setVisibility(View.GONE);
                if (rvRecentSms != null) {
                    rvRecentSms.setVisibility(View.VISIBLE);
                    // Take top 5
                    List<com.smartexpense.mobile.model.Transaction> top5 = new ArrayList<>();
                    for (int i = 0; i < Math.min(5, transactions.size()); i++) {
                        top5.add(transactions.get(i));
                    }
                    rvRecentSms.setAdapter(new com.smartexpense.mobile.sms.SmsAdapter(top5));
                }
            }
        });
    }

    // Methods removed for auto-adding

    private void fetchChartData() {
        if (pieChartHome == null || uid == null) return;
        com.smartexpense.mobile.database.AppDatabase roomDb = com.smartexpense.mobile.database.AppDatabase.getDatabase(getContext());
        roomDb.transactionDao().getAllTransactions(uid).observe(getViewLifecycleOwner(), transactions -> {
            if (transactions == null) return;
            java.util.Map<String, Double> categoryTotals = new java.util.HashMap<>();
            for (com.smartexpense.mobile.model.Transaction tx : transactions) {
                if ("DEBIT".equalsIgnoreCase(tx.type)) {
                    categoryTotals.put(tx.category, 
                        categoryTotals.getOrDefault(tx.category, 0.0) + tx.amount);
                }
            }
            if (categoryTotals.isEmpty()) categoryTotals.put("No Data", 1.0);

            List<com.github.mikephil.charting.data.PieEntry> entries = new ArrayList<>();
            for (java.util.Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                entries.add(new com.github.mikephil.charting.data.PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }

            com.github.mikephil.charting.data.PieDataSet dataSet = new com.github.mikephil.charting.data.PieDataSet(entries, "");
            dataSet.setColors(com.github.mikephil.charting.utils.ColorTemplate.MATERIAL_COLORS);
            dataSet.setValueTextColor(android.graphics.Color.BLACK);
            dataSet.setValueTextSize(12f);

            pieChartHome.setData(new com.github.mikephil.charting.data.PieData(dataSet));
            pieChartHome.invalidate();
        });
    }

    private void showEditBalanceSheet() {
        if (db == null || uid == null) return;
        
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            Double currentSavings = doc.getDouble("savingsBalance");
            Double currentMF = doc.getDouble("mutualFunds");
            
            EditBalanceBottomSheet.newInstance(
                currentSavings != null ? currentSavings : 0,
                currentMF != null ? currentMF : 0,
                this::fetchUserData
            ).show(getChildFragmentManager(), "edit_balance");
        });
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(amount);
    }
}
