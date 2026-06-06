package com.smartexpense.mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

public class BillsFragment extends Fragment implements BillAdapter.OnBillActionListener {

    private RecyclerView rvBills;
    private FloatingActionButton fabAdd;
    private android.widget.ProgressBar progressBar;
    private android.widget.TextView tvEmptyBills;
    private BillAdapter adapter;
    private String uid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bills, container, false);

        rvBills = view.findViewById(R.id.rvBills);
        fabAdd = view.findViewById(R.id.fabAddBill);
        progressBar = view.findViewById(R.id.progressBar);
        tvEmptyBills = view.findViewById(R.id.tvEmptyBills);

        rvBills.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BillAdapter(this);
        rvBills.setAdapter(adapter);

        android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
        uid = prefs.getString("userId", null);
        
        if (uid != null) {
            fetchBills();
        }

        fabAdd.setOnClickListener(v -> {
            AddBillBottomSheet sheet = new AddBillBottomSheet();
            sheet.setOnDismissListener(this::fetchBills);
            sheet.show(getChildFragmentManager(), "add_bill");
        });

        return view;
    }

    private void fetchBills() {
        if (uid == null) return;
        progressBar.setVisibility(View.VISIBLE);
        tvEmptyBills.setVisibility(View.GONE);
        com.smartexpense.mobile.network.RetrofitClient.getClient().create(com.smartexpense.mobile.network.ApiService.class).getBills(uid)
            .enqueue(new retrofit2.Callback<List<com.smartexpense.mobile.model.Bill>>() {
                @Override
                public void onResponse(retrofit2.Call<List<com.smartexpense.mobile.model.Bill>> call, retrofit2.Response<List<com.smartexpense.mobile.model.Bill>> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.submitList(response.body());
                        if (response.body().isEmpty()) {
                            tvEmptyBills.setVisibility(View.VISIBLE);
                        } else {
                            tvEmptyBills.setVisibility(View.GONE);
                        }
                    } else {
                        tvEmptyBills.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<List<com.smartexpense.mobile.model.Bill>> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    tvEmptyBills.setVisibility(View.VISIBLE);
                }
            });
    }

    @Override
    public void onPay(com.smartexpense.mobile.model.Bill bill) {
        if (uid == null) return;
        progressBar.setVisibility(View.VISIBLE);
        com.smartexpense.mobile.network.RetrofitClient.getClient().create(com.smartexpense.mobile.network.ApiService.class).markBillAsPaid(bill.id)
            .enqueue(new retrofit2.Callback<com.smartexpense.mobile.model.Bill>() {
                @Override
                public void onResponse(retrofit2.Call<com.smartexpense.mobile.model.Bill> call, retrofit2.Response<com.smartexpense.mobile.model.Bill> response) {
                    progressBar.setVisibility(View.GONE);
                    if (response.isSuccessful()) {
                        Toast.makeText(getContext(), "Bill marked as paid", Toast.LENGTH_SHORT).show();
                        fetchBills();
                    } else {
                        Toast.makeText(getContext(), "Failed to pay bill", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(retrofit2.Call<com.smartexpense.mobile.model.Bill> call, Throwable t) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
