package com.smartexpense.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.smartexpense.mobile.network.RetrofitClient;
import com.smartexpense.mobile.network.ApiService;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvError, tvModeInfo;
    private TextInputLayout nameLayout;
    private LinearLayout modeInfo;
    private boolean isSignupMode = false;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("ExpenseTracker", MODE_PRIVATE);

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvError = findViewById(R.id.tvError);
        tvModeInfo = findViewById(R.id.tvModeInfo);
        nameLayout = findViewById(R.id.nameLayout);
        modeInfo = findViewById(R.id.modeInfo);

        tvSignup.setOnClickListener(v -> {
            isSignupMode = !isSignupMode;
            updateUIForMode();
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                String name = etName.getText().toString().trim();

                hideError();

                if (email.isEmpty() || password.isEmpty()) {
                    showError("Please fill all required fields");
                    return;
                }

                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    showError("Please enter a valid email address");
                    return;
                }

                if (password.length() < 6) {
                    showError("Password must be at least 6 characters");
                    return;
                }

                if (isSignupMode && name.isEmpty()) {
                    showError("Please enter your full name");
                    return;
                }

                ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
                Map<String, String> data = new HashMap<>();
                data.put("email", email);
                data.put("password", password);
                if (isSignupMode) {
                    data.put("name", name);
                }

                btnLogin.setEnabled(false);
                btnLogin.setText(isSignupMode ? "Creating Account..." : "Logging In...");
                Toast.makeText(LoginActivity.this, isSignupMode ? "Starting Signup..." : "Starting Login...", Toast.LENGTH_SHORT).show();

                if (isSignupMode) {
                    performSignup(apiService, data);
                } else {
                    performLogin(apiService, data);
                }
            }
        });

        // Check if user is already logged in via Firebase
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void updateUIForMode() {
        if (isSignupMode) {
            nameLayout.setVisibility(View.VISIBLE);
            btnLogin.setText("Sign Up");
            tvSignup.setText("Back to Login");
            tvModeInfo.setText("Already have an account? Tap 'Back to Login' to switch");
        } else {
            nameLayout.setVisibility(View.GONE);
            btnLogin.setText("Login");
            tvSignup.setText("Don't have an account? Sign Up");
            tvModeInfo.setText("New user? Tap 'Sign Up' to create an account");
        }
        etName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        hideError();
    }

    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvError.setVisibility(View.GONE);
    }

    private void performSignup(ApiService apiService, Map<String, String> data) {
        String email = data.get("email");
        String password = data.get("password");
        String name = data.get("name");

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            Map<String, Object> profile = new HashMap<>();
                            profile.put("name", name);
                            profile.put("email", email);
                            profile.put("savingsBalance", 0.0);
                            profile.put("mutualFunds", 0.0);
                            profile.put("onboardingCompleted", false);

                            FirebaseFirestore.getInstance().collection("users")
                                    .document(user.getUid()).set(profile)
                                    .addOnSuccessListener(aVoid -> {
                                        btnLogin.setEnabled(true);
                                        btnLogin.setText("Sign Up");
                                        Toast.makeText(LoginActivity.this, "✓ Account Created!", Toast.LENGTH_SHORT).show();
                                        isSignupMode = false;
                                        updateUIForMode();
                                    });
                        }
                    } else {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Sign Up");
                        showError("Signup Failed: " + task.getException().getMessage());
                    }
                });
    }

    private void performLogin(ApiService apiService, Map<String, String> data) {
        String email = data.get("email");
        String password = data.get("password");

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Login");
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "✓ Welcome back!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        showError("Login Failed: " + task.getException().getMessage());
                    }
                });
    }
}
