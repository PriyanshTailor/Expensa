package com.smartexpense.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.Locale;

import com.smartexpense.mobile.model.Transaction;

public class ExpenseAdapter extends ListAdapter<Transaction, ExpenseAdapter.ViewHolder> {

    public ExpenseAdapter() {
        super(new DiffUtil.ItemCallback<Transaction>() {
            @Override
            public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                return oldItem.amount == newItem.amount && 
                       (oldItem.merchant != null && oldItem.merchant.equals(newItem.merchant));
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction expense = getItem(position);
        String displayTitle = (expense.merchant != null && !expense.merchant.trim().isEmpty()) ? expense.merchant : expense.category;
        if (displayTitle == null || displayTitle.trim().isEmpty()) displayTitle = "Unknown";
        holder.tvMerchant.setText(displayTitle);
        holder.tvCategory.setText(expense.category);
        
        String amountStr = NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(expense.amount);
        holder.tvAmount.setText(amountStr);
        
        if ("debit".equalsIgnoreCase(expense.type)) {
            holder.tvAmount.setTextColor(0xFFEF4444); // debit color
            holder.tvAmount.setText("-" + amountStr);
        } else {
            holder.tvAmount.setTextColor(0xFF22C55E); // credit color
            holder.tvAmount.setText("+" + amountStr);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMerchant, tvCategory, tvAmount;
        ImageView ivCategoryIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMerchant = itemView.findViewById(R.id.tvMerchant);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            ivCategoryIcon = itemView.findViewById(R.id.ivCategoryIcon);
        }
    }
}
