package com.smartexpense.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Map<String, Object>> transactions;

    public TransactionAdapter(List<Map<String, Object>> transactions) {
        this.transactions = transactions;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> transaction = transactions.get(position);
        holder.tvTitle.setText((String) transaction.get("title"));
        holder.tvCategory.setText((String) transaction.get("category"));
        
        Number amountNum = (Number) transaction.get("amount");
        double amount = amountNum != null ? amountNum.doubleValue() : 0.0;
        String type = (String) transaction.get("type");
        
        if ("debit".equalsIgnoreCase(type)) {
            holder.tvAmount.setText("-₹" + amount);
            holder.tvAmount.setTextColor(0xFFE74C3C);
        } else {
            holder.tvAmount.setText("+₹" + amount);
            holder.tvAmount.setTextColor(0xFF27AE60);
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvAmount;
        ImageView ivIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivIcon = itemView.findViewById(R.id.ivTransactionIcon);
        }
    }
}
