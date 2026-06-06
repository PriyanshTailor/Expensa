package com.smartexpense.mobile;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private BarChart barChart;
    private PieChart pieChart;
    
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        barChart = view.findViewById(R.id.barChart);
        pieChart = view.findViewById(R.id.pieChart);
        
        try {
            
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
            uid = prefs.getString("userId", null);
            if (uid != null) {
                fetchData();
            }
        } catch (Exception e) {
            // Firebase not initialized
        }

        setupCharts();

        return view;
    }

    private void setupCharts() {
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        
        pieChart.getDescription().setEnabled(false);
        pieChart.setHoleRadius(40f);
        pieChart.setTransparentCircleRadius(45f);
        pieChart.setDrawEntryLabels(false);
    }

    private void fetchData() {
        /* Firebase Removed */
        // Data fetch removed for compilation
    }

    private void updatePieChart(Map<String, Double> categoryTotals) {
        List<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.BLACK);
        dataSet.setValueTextSize(12f);

        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate();
    }

    private void updateBarChart(double income, double expense) {
        List<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0f, (float) income));
        entries.add(new BarEntry(1f, (float) expense));

        BarDataSet dataSet = new BarDataSet(entries, "Income vs Expense");
        dataSet.setColors(new int[]{0xFF22C55E, 0xFFEF4444}); // green, red
        
        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.invalidate();
    }
}
