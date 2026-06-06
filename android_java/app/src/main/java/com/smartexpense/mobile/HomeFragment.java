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

import com.smartexpense.mobile.network.ApiService;
import com.smartexpense.mobile.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;

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

    private String uid;
    private com.github.mikephil.charting.charts.PieChart pieChartHome;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        setGreeting();

        try {
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
            if (prefs.getBoolean("isLoggedIn", false)) {
                uid = prefs.getString("userId", null);
                String userName = prefs.getString("userName", "User");
                tvUserName.setText(userName);
                

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
                AddExpenseBottomSheet sheet = new AddExpenseBottomSheet();
                sheet.setOnDismissListener(() -> {
                    fetchUserData();
                    fetchRecentSms();
                    fetchChartData();
                });
                sheet.show(getParentFragmentManager(), "add_expense");
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
        if (uid == null) return;

        android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
        String userName = prefs.getString("userName", "User");
        tvUserName.setText(userName);
        
        RetrofitClient.getClient().create(ApiService.class).getUserProfile(uid)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> data = response.body();
                        String name = (String) data.get("name");
                        Double savings = data.get("savingsBalance") instanceof Number ? ((Number) data.get("savingsBalance")).doubleValue() : 0.0;
                        Double mf = data.get("mutualFunds") instanceof Number ? ((Number) data.get("mutualFunds")).doubleValue() : 0.0;

                        if (name != null) tvUserName.setText(name);
                        
                        tvSavingsBalance.setText(formatCurrency(savings));
                        tvMFBalance.setText(formatCurrency(mf));
                        tvTotalWealth.setText(formatCurrency(savings + mf));

                        if (btnAddUpdateWealth != null) {
                            if (savings > 0 || mf > 0) {
                                btnAddUpdateWealth.setText("Update");
                            } else {
                                btnAddUpdateWealth.setText("Add");
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
            });
    }

    private void fetchRecentSms() {
        if (uid == null) return;
        
        RetrofitClient.getClient().create(ApiService.class).getRecentTransactions()
            .enqueue(new Callback<List<Map<String, Object>>>() {
                @Override
                public void onResponse(Call<List<Map<String, Object>>> call, Response<List<Map<String, Object>>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Map<String, Object>> transactions = response.body();
                        if (transactions.isEmpty()) {
                            if (layoutEmptySms != null) layoutEmptySms.setVisibility(View.VISIBLE);
                            if (rvRecentSms != null) rvRecentSms.setVisibility(View.GONE);
                        } else {
                            if (layoutEmptySms != null) layoutEmptySms.setVisibility(View.GONE);
                            if (rvRecentSms != null) {
                                rvRecentSms.setVisibility(View.VISIBLE);
                                List<com.smartexpense.mobile.model.Transaction> top5 = new ArrayList<>();
                                for (Map<String, Object> doc : transactions) {
                                    com.smartexpense.mobile.model.Transaction tx = new com.smartexpense.mobile.model.Transaction();
                                    tx.amount = doc.get("amount") instanceof Number ? ((Number) doc.get("amount")).doubleValue() : 0.0;
                                    tx.merchant = (String) doc.get("merchant");
                                    tx.category = (String) doc.get("category");
                                    tx.type = (String) doc.get("type");
                                    tx.source = (String) doc.get("source");
                                    // Parse date string to long if necessary, assuming backend sends epoch or ISO string
                                    Object dateObj = doc.get("date");
                                    if (dateObj instanceof Number) {
                                        tx.timestamp = ((Number) dateObj).longValue();
                                    } else if (dateObj instanceof String) {
                                        try {
                                            java.time.Instant instant = java.time.Instant.parse((String) dateObj);
                                            tx.timestamp = instant.toEpochMilli();
                                        } catch(Exception e){}
                                    }
                                    tx.rawSms = (String) doc.get("rawMessage");
                                    if (tx.rawSms == null) tx.rawSms = "";
                                    top5.add(tx);
                                }
                                rvRecentSms.setAdapter(new com.smartexpense.mobile.sms.SmsAdapter(top5));
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Map<String, Object>>> call, Throwable t) {}
            });
    }

    // Methods removed for auto-adding

    private void fetchChartData() {
        if (pieChartHome == null || uid == null) return;
        
        RetrofitClient.getClient().create(ApiService.class).getTransactionSummary()
            .enqueue(new Callback<Map<String, Double>>() {
                @Override
                public void onResponse(Call<Map<String, Double>> call, Response<Map<String, Double>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Double> categoryTotals = response.body();
                        
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
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Double>> call, Throwable t) {}
            });
    }

    private void showEditBalanceSheet() {
        if (uid == null) return;
        
        RetrofitClient.getClient().create(ApiService.class).getUserProfile(uid)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> data = response.body();
                        Double currentSavings = data.get("savingsBalance") instanceof Number ? ((Number) data.get("savingsBalance")).doubleValue() : 0.0;
                        Double currentMF = data.get("mutualFunds") instanceof Number ? ((Number) data.get("mutualFunds")).doubleValue() : 0.0;
                        
                        EditBalanceBottomSheet.newInstance(
                            currentSavings,
                            currentMF,
                            () -> fetchUserData()
                        ).show(getChildFragmentManager(), "edit_balance");
                    }
                }
                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
            });
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(amount);
    }
}
