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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TripsFragment extends Fragment {

    private RecyclerView rvTripsList;
    private TripListAdapter adapter;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trips_list, container, false);

        rvTripsList = view.findViewById(R.id.rvTripsList);
        rvTripsList.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new TripListAdapter(new TripListAdapter.OnTripClickListener() {
            @Override
            public void onTripClick(TripListAdapter.TripObj trip) {
                TripDetailFragment detailFrag = new TripDetailFragment();
                Bundle args = new Bundle();
                args.putString("TRIP_ID", trip.id);
                args.putString("TRIP_NAME", trip.name);
                detailFrag.setArguments(args);
                
                requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFrag)
                    .addToBackStack(null)
                    .commit();
            }

            @Override
            public void onTripLongClick(TripListAdapter.TripObj trip) {
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("Delete Notebook")
                    .setMessage("Are you sure you want to delete '" + trip.name + "'?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        db.collection("trips").document(trip.id).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        });
        rvTripsList.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadTrips();

        view.findViewById(R.id.fabAddTrip).setOnClickListener(v -> {
            new AddTripBottomSheet().show(getChildFragmentManager(), "add_new_trip");
        });

        return view;
    }

    private void loadTrips() {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("trips")
            .whereEqualTo("createdBy", uid)
            .addSnapshotListener((value, error) -> {
            if (error != null) {
                if (getContext() != null) Toast.makeText(getContext(), "Error loading trips", Toast.LENGTH_SHORT).show();
                return;
            }
            if (value != null) {
                List<TripListAdapter.TripObj> trips = new ArrayList<>();
                for (QueryDocumentSnapshot doc : value) {
                    String name = doc.getString("name");
                    if (name == null) name = "Trip";
                    Map<String, Object> membersMap = (Map<String, Object>) doc.get("members");
                    int count = membersMap != null ? membersMap.size() : 0;
                    trips.add(new TripListAdapter.TripObj(doc.getId(), name, count));
                }
                adapter.submitList(trips);
            }
        });
    }
}
