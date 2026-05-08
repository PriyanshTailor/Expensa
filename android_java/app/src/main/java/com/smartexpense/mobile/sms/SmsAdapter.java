package com.smartexpense.mobile.sms;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.smartexpense.mobile.R;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.smartexpense.mobile.model.Transaction;

public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.ViewHolder> {

    private List<Transaction> transactions;

    public SmsAdapter(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_expense, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction tx = transactions.get(position);
        
        holder.tvMerchant.setText(tx.merchant);
        
        if (tx.rawSms != null && !tx.rawSms.isEmpty()) {
            holder.tvCategory.setText(tx.category + " • " + tx.rawSms);
            holder.tvCategory.setMaxLines(2);
            holder.tvCategory.setEllipsize(android.text.TextUtils.TruncateAt.END);
        } else {
            holder.tvCategory.setText(tx.category);
        }
        
        String amountStr = NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(tx.amount);
        holder.tvAmount.setText(amountStr);

        if ("debit".equalsIgnoreCase(tx.type)) {
            holder.tvAmount.setTextColor(0xFFEF4444);
            holder.tvAmount.setText("-" + amountStr);
        } else {
            holder.tvAmount.setTextColor(0xFF22C55E);
            holder.tvAmount.setText("+" + amountStr);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
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
