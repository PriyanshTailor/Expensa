package com.smartexpense.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.smartexpense.mobile.network.ApiService;
import com.smartexpense.mobile.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Map;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        Button btnLogout = view.findViewById(R.id.btnLogout);
        Button btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);


        fetchProfile();

        btnLogout.setOnClickListener(v -> {
            if (getContext() != null) {
                getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE).edit().clear().apply();
                com.smartexpense.mobile.network.RetrofitClient.setAuthToken(null);
            }
            startActivity(new Intent(getContext(), LoginActivity.class));
            if (getActivity() != null) getActivity().finish();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            if (getContext() == null) return;
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? This will permanently delete your profile data from cloud.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
                    String uid = prefs.getString("userId", null);
                    if (uid != null) {
                        prefs.edit().clear().apply();
                        com.smartexpense.mobile.network.RetrofitClient.setAuthToken(null);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Account data cleared locally", Toast.LENGTH_SHORT).show();
                        }
                        startActivity(new Intent(getContext(), LoginActivity.class));
                        if (getActivity() != null) getActivity().finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        return view;
    }

    private void fetchProfile() {
        if (getContext() == null) return;
        try {
            android.content.SharedPreferences prefs = getContext().getSharedPreferences("ExpenseTracker", android.content.Context.MODE_PRIVATE);
            String uid = prefs.getString("userId", null);
            String email = prefs.getString("userEmail", null);
            String name = prefs.getString("userName", null);

            if (name != null) tvName.setText(name);
            if (email != null) tvEmail.setText(email);

            if (uid != null) {
                RetrofitClient.getClient().create(ApiService.class).getUserProfile(uid)
                    .enqueue(new Callback<Map<String, Object>>() {
                        @Override
                        public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Map<String, Object> data = response.body();
                                String n = (String) data.get("name");
                                String em = (String) data.get("email");
                                if (n != null) tvName.setText(n);
                                if (em != null) tvEmail.setText(em);
                            }
                        }
                        @Override
                        public void onFailure(Call<Map<String, Object>> call, Throwable t) {}
                    });
            }
        } catch (Exception e) {
            tvName.setText("Config Error");
            tvEmail.setText("Check Database");
        }
    }
}
