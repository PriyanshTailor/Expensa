package com.smartexpense.mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

public class TripListAdapter extends ListAdapter<TripListAdapter.TripObj, TripListAdapter.ViewHolder> {

    public interface OnTripClickListener {
        void onTripClick(TripObj trip);
        void onTripLongClick(TripObj trip);
    }

    private OnTripClickListener listener;

    public TripListAdapter(OnTripClickListener listener) {
        super(new DiffUtil.ItemCallback<TripObj>() {
            @Override
            public boolean areItemsTheSame(@NonNull TripObj oldItem, @NonNull TripObj newItem) {
                return oldItem.id.equals(newItem.id);
            }

            @Override
            public boolean areContentsTheSame(@NonNull TripObj oldItem, @NonNull TripObj newItem) {
                return oldItem.name.equals(newItem.name);
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
        TripObj trip = getItem(position);
        holder.tvName.setText(trip.name);
        holder.tvMembers.setText(trip.memberCount + " Members");
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

    public static class TripObj {
        public String id;
        public String name;
        public int memberCount;
        public TripObj(String id, String name, int memberCount) {
            this.id = id;
            this.name = name;
            this.memberCount = memberCount;
        }
    }
}
