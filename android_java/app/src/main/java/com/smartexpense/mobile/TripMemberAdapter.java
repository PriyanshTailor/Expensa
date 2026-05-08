package com.smartexpense.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.util.Locale;

public class TripMemberAdapter extends ListAdapter<TripDetailFragment.TripMember, TripMemberAdapter.ViewHolder> {

    public TripMemberAdapter() {
        super(new DiffUtil.ItemCallback<TripDetailFragment.TripMember>() {
            @Override
            public boolean areItemsTheSame(@NonNull TripDetailFragment.TripMember oldItem, @NonNull TripDetailFragment.TripMember newItem) {
                return oldItem.name.equals(newItem.name);
            }

            @Override
            public boolean areContentsTheSame(@NonNull TripDetailFragment.TripMember oldItem, @NonNull TripDetailFragment.TripMember newItem) {
                return oldItem.spent == newItem.spent;
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TripDetailFragment.TripMember member = getItem(position);
        holder.tvName.setText(member.name);
        holder.tvSpent.setText(NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(member.spent));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvSpent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMemberName);
            tvSpent = itemView.findViewById(R.id.tvMemberSpent);
        }
    }
}
