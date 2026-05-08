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
import androidx.lifecycle.ViewModelProvider;
import com.smartexpense.mobile.model.Bill;
import com.smartexpense.mobile.model.Transaction;
import com.smartexpense.mobile.viewmodel.ExpenseViewModel;
import com.smartexpense.mobile.database.AppDatabase;

import java.util.ArrayList;
import java.util.List;

public class BillsFragment extends Fragment implements BillAdapter.OnBillActionListener {

    private RecyclerView rvBills;
    private FloatingActionButton fabAdd;
    private ExpenseViewModel expenseViewModel;
    private BillAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bills, container, false);

        rvBills = view.findViewById(R.id.rvBills);
        fabAdd = view.findViewById(R.id.fabAddBill);

        rvBills.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new BillAdapter(this);
        rvBills.setAdapter(adapter);

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        expenseViewModel.getAllBills().observe(getViewLifecycleOwner(), bills -> {
            if (bills != null) {
                // Filter to only show upcoming bills (isPaid == false)
                List<Bill> upcomingBills = new ArrayList<>();
                for (Bill b : bills) {
                    if (!b.isPaid) upcomingBills.add(b);
                }
                adapter.submitList(upcomingBills);
            }
        });

        fabAdd.setOnClickListener(v -> {
            new AddBillBottomSheet().show(getChildFragmentManager(), "add_bill");
        });

        return view;
    }

    @Override
    public void onPay(Bill bill) {
        new Thread(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(getContext());
                
                // Mark as paid
                bill.isPaid = true;
                db.billDao().update(bill);
                
                // Automatically add it as an expense transaction
                Transaction t = new Transaction();
                t.amount = bill.amountDue;
                t.category = bill.billType;
                t.merchant = bill.billerName;
                t.type = "DEBIT";
                t.source = "BILL_PAY";
                t.timestamp = System.currentTimeMillis();
                t.pendingReview = false;
                t.rawSms = "Paid bill: " + bill.billerName;
                t.userId = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
                
                db.transactionDao().insert(t);
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> 
                        Toast.makeText(getContext(), "Bill marked as paid!", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {}
        }).start();
    }
}
