package com.smartexpense.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private Button btnLogout, btnDeleteAccount;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvName = view.findViewById(R.id.tvProfileName);
        tvEmail = view.findViewById(R.id.tvProfileEmail);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);


        fetchProfile();

        btnLogout.setOnClickListener(v -> {
            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
            } catch (Exception e) {}
            startActivity(new Intent(getContext(), LoginActivity.class));
            if (getActivity() != null) getActivity().finish();
        });

        btnDeleteAccount.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? This will permanently delete your profile and all manual expenses.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
                    if (user != null) {
                        String uid = user.getUid();
                        // Delete Firestore data
                        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid).delete();
                        // Delete Auth account
                        user.delete().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                android.widget.Toast.makeText(getContext(), "Account Deleted", android.widget.Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getContext(), LoginActivity.class));
                                if (getActivity() != null) getActivity().finish();
                            } else {
                                android.widget.Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
        });

        return view;
    }

    private void fetchProfile() {
        try {
            com.google.firebase.auth.FirebaseUser user = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
            if (user != null) {
                if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
                    tvName.setText(user.getDisplayName());
                }
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    tvEmail.setText(user.getEmail());
                }

                String uid = user.getUid();
                com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                        .addSnapshotListener((doc, e) -> {
                            if (doc != null && doc.exists()) {
                                String name = doc.getString("name");
                                String email = doc.getString("email");
                                if (name != null) tvName.setText(name);
                                if (email != null) tvEmail.setText(email);
                            }
                        });
            }
        } catch (Exception e) {
            tvName.setText("Config Error");
            tvEmail.setText("Check Database");
        }
    }
}
