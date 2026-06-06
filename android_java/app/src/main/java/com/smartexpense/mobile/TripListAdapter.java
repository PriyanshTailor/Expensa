package com.smartexpense.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.smartexpense.mobile.model.Trip;

public class TripListAdapter extends ListAdapter<Trip, TripListAdapter.ViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(Trip trip);
        void onTripLongClick(Trip trip);
    }

    private OnTripClickListener listener;

    public TripListAdapter(OnTripClickListener listener) {
        super(new DiffUtil.ItemCallback<Trip>() {
            @Override
            public boolean areItemsTheSame(@NonNull Trip oldItem, @NonNull Trip newItem) {
                String oldId = oldItem.getId();
                String newId = newItem.getId();
                if (oldId == null || newId == null) return false;
                return oldId.equals(newId);
            }

            @Override
            public boolean areContentsTheSame(@NonNull Trip oldItem, @NonNull Trip newItem) {
                String oldName = oldItem.getName();
                String newName = newItem.getName();
                if (oldName == null || newName == null) return false;
                return oldName.equals(newName) &&
                       oldItem.getMemberCount() == newItem.getMemberCount();
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_trip, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Trip trip = getItem(position);
        holder.tvName.setText(trip.getName());
        holder.tvMembers.setText(trip.getMemberCount() + " Members");
        holder.itemView.setOnClickListener(v -> listener.onTripClick(trip));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onTripLongClick(trip);
            return true;
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvMembers;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvTripName);
            tvMembers = itemView.findViewById(R.id.tvTripMembersCount);
        }
    }
}
