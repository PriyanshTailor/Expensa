package com.smartexpense.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.smartexpense.mobile.network.RetrofitClient;
import com.smartexpense.mobile.network.ApiService;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvError, tvModeInfo, tvServerConfig;
    private TextInputLayout nameLayout;
    private LinearLayout modeInfo;
    private boolean isSignupMode = false;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sharedPreferences = getSharedPreferences("ExpenseTracker", MODE_PRIVATE);
        
        String savedIp = sharedPreferences.getString("serverIp", null);
        if (savedIp != null && !savedIp.isEmpty()) {
            RetrofitClient.setCustomBaseUrl("http://" + savedIp + ":8080/");
        }

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        tvError = findViewById(R.id.tvError);
        tvModeInfo = findViewById(R.id.tvModeInfo);
        tvServerConfig = findViewById(R.id.tvServerConfig);
        nameLayout = findViewById(R.id.nameLayout);
        modeInfo = findViewById(R.id.modeInfo);

        tvServerConfig.setOnClickListener(v -> showServerConfigDialog());

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

                btnLogin.setEnabled(false);
                btnLogin.setText(isSignupMode ? "Creating Account..." : "Logging In...");
                Toast.makeText(LoginActivity.this, isSignupMode ? "Starting Signup..." : "Starting Login...", Toast.LENGTH_SHORT).show();

                if (isSignupMode) {
                    performSignup(email, password, name);
                } else {
                    performLogin(email, password);
                }
            }
        });

        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            String token = sharedPreferences.getString("token", null);
            if (token != null) {
                RetrofitClient.setAuthToken(token);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
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

    private void showServerConfigDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Server IP Address");
        builder.setMessage("Enter the IP address of your backend server (e.g., 10.163.135.74)");
        
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        String currentIp = sharedPreferences.getString("serverIp", "");
        input.setText(currentIp);
        builder.setView(input);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            String ip = input.getText().toString().trim();
            if (!ip.isEmpty()) {
                sharedPreferences.edit().putString("serverIp", ip).apply();
                RetrofitClient.setCustomBaseUrl("http://" + ip + ":8080/");
                Toast.makeText(this, "Server IP updated to " + ip, Toast.LENGTH_SHORT).show();
            } else {
                sharedPreferences.edit().remove("serverIp").apply();
                RetrofitClient.setCustomBaseUrl(null);
                Toast.makeText(this, "Server IP reset to default", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void performSignup(String email, String password, String name) {
        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);
        request.put("name", name);

        RetrofitClient.getClient().create(ApiService.class).register(request)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        // After successful signup, immediately log them in to get the JWT token
                        performLogin(email, password);
                    } else {
                        btnLogin.setEnabled(true);
                        btnLogin.setText("Sign Up");
                        showError("Signup Failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText("Sign Up");
                    showError("Network Error: " + t.getMessage());
                }
            });
    }

    private void performLogin(String email, String password) {
        Map<String, String> request = new HashMap<>();
        request.put("email", email);
        request.put("password", password);

        RetrofitClient.getClient().create(ApiService.class).login(request)
            .enqueue(new Callback<Map<String, Object>>() {
                @Override
                public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(isSignupMode ? "Sign Up" : "Login");

                    if (response.isSuccessful() && response.body() != null) {
                        Map<String, Object> body = response.body();
                        String token = (String) body.get("token");
                        
                        // User map parsing
                        Map<String, Object> userMap = null;
                        if (body.get("user") instanceof Map) {
                            userMap = (Map<String, Object>) body.get("user");
                        }
                        
                        String userId = userMap != null && userMap.get("id") != null ? String.valueOf(userMap.get("id")) : "Unknown";
                        String userName = userMap != null && userMap.get("name") != null ? String.valueOf(userMap.get("name")) : "User";

                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("token", token);
                        editor.putBoolean("isLoggedIn", true);
                        editor.putString("userId", userId);
                        editor.putString("userName", userName);
                        editor.putString("userEmail", email);
                        editor.apply();

                        RetrofitClient.setAuthToken(token);

                        Toast.makeText(LoginActivity.this, "✓ Welcome " + userName + "!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        showError("Invalid email or password");
                    }
                }

                @Override
                public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                    btnLogin.setEnabled(true);
                    btnLogin.setText(isSignupMode ? "Sign Up" : "Login");
                    showError("Network Error: " + t.getMessage());
                }
            });
    }
}
