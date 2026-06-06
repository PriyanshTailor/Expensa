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

import com.smartexpense.mobile.network.ApiService;
import com.smartexpense.mobile.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;

import com.smartexpense.mobile.model.Trip;

import java.util.ArrayList;
import java.util.List;

public class TripsFragment extends Fragment {

    private TripListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trips_list, container, false);

        RecyclerView rvTripsList = view.findViewById(R.id.rvTripsList);
        rvTripsList.setLayoutManager(new LinearLayoutManager(getContext()));
        


        adapter = new TripListAdapter(new TripListAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(Trip trip) {
                TripDetailFragment detailFrag = new TripDetailFragment();
                Bundle args = new Bundle();
                args.putString("TRIP_ID", trip.getId());
                args.putString("TRIP_NAME", trip.getName());
                detailFrag.setArguments(args);
                
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFrag)
                    .addToBackStack(null)
                    .commit();
            }

            @Override
            public void onTripLongClick(Trip trip) {
                if (getContext() == null) return;
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Delete Notebook")
                    .setMessage("Are you sure you want to delete '" + trip.getName() + "'?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        RetrofitClient.getClient().create(ApiService.class).deleteTrip(trip.getId())
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(Call<Void> call, Response<Void> response) {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "✓ Notebook deleted", Toast.LENGTH_SHORT).show();
                                    }
                                    loadTrips();
                                }

                                @Override
                                public void onFailure(Call<Void> call, Throwable t) {
                                    if (getContext() != null) {
                                        Toast.makeText(getContext(), "Delete failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                        android.util.Log.e("TripsFragment", "Delete error", t);
                                    }
                                }
                            });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        rvTripsList.setAdapter(adapter);

        view.findViewById(R.id.btnRefreshTrips).setOnClickListener(v -> loadTrips());

        loadTrips();

        view.findViewById(R.id.fabAddTrip).setOnClickListener(v -> {
            AddTripBottomSheet sheet = new AddTripBottomSheet();
            sheet.setOnDismissListener(this::loadTrips);
            sheet.show(getChildFragmentManager(), "add_new_trip");
        });

        return view;
    }

    private void loadTrips() {
        if (getContext() == null) return;
        android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
        String uid = prefs.getString("userId", null);
        if (uid == null) return;
        
        RetrofitClient.getClient().create(ApiService.class).getTrips(uid)
            .enqueue(new Callback<List<Trip>>() {
                @Override
                public void onResponse(Call<List<Trip>> call, Response<List<Trip>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        adapter.submitList(response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<Trip>> call, Throwable t) {}
            });
    }
}
