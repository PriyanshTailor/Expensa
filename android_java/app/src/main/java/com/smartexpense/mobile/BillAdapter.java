package com.smartexpense.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.Locale;

import com.smartexpense.mobile.model.Bill;

public class BillAdapter extends ListAdapter<Bill, BillAdapter.ViewHolder> {

    private OnBillActionListener listener;

    public interface OnBillActionListener {
        void onPay(Bill bill);
    }

    public BillAdapter(OnBillActionListener listener) {
        super(new DiffUtil.ItemCallback<Bill>() {
            @Override
            public boolean areItemsTheSame(@NonNull Bill oldItem, @NonNull Bill newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Bill oldItem, @NonNull Bill newItem) {
                return oldItem.isPaid == newItem.isPaid &&
                       oldItem.amountDue == newItem.amountDue;
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Bill bill = getItem(position);
        holder.tvName.setText(bill.billerName);
        holder.tvDue.setText("Due on " + (bill.dueDate != null ? bill.dueDate : "N/A"));
        holder.tvAmount.setText(NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(bill.amountDue));
        holder.tvStatus.setText(bill.isPaid ? "PAID" : "UPCOMING");

        if (bill.isPaid) {
            holder.btnPay.setVisibility(View.GONE);
            holder.tvStatus.setBackgroundColor(0xFFDCFCE7);
            holder.tvStatus.setTextColor(0xFF166534);
        } else {
            holder.btnPay.setVisibility(View.VISIBLE);
            holder.btnPay.setOnClickListener(v -> listener.onPay(bill));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDue, tvAmount, tvStatus;
        Button btnPay;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvBillName);
            tvDue = itemView.findViewById(R.id.tvDueDate);
            tvAmount = itemView.findViewById(R.id.tvBillAmount);
            tvStatus = itemView.findViewById(R.id.tvBillStatus);
            btnPay = itemView.findViewById(R.id.btnPayBill);
        }
    }
}
