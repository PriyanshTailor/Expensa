package com.smartexpense.mobile;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
// import com.google.firebase.auth.FirebaseAuth;
// import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int SMS_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Auth Check (Using Firebase)
        try {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return;
            }
        } catch (Exception e) {
            android.util.Log.e("MainActivity", "Firebase Config Error", e);
            // If Firebase is failing, we redirect to Login where we can show a better error
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        com.google.android.material.appbar.MaterialToolbar topToolbar = findViewById(R.id.topToolbar);
        topToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_profile) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                return true;
            } else if (item.getItemId() == R.id.action_learn) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new LearnFragment()).commit();
                return true;
            } else if (item.getItemId() == R.id.action_logout) {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                return true;
            }
            return false;
        });

        requestSmsPermission();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        
        // Set default fragment only if this is the first creation
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_expenses) {
                selectedFragment = new ExpensesFragment();
            } else if (itemId == R.id.nav_bills) {
                selectedFragment = new BillsFragment();
            } else if (itemId == R.id.nav_invest) {
                selectedFragment = new InvestFragment();
            } else if (itemId == R.id.nav_trips) {
                selectedFragment = new TripsFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
            }
            return true;
        });
    }

    private void requestSmsPermission() {
        java.util.List<String> permissions = new java.util.ArrayList<>();
        permissions.add(Manifest.permission.READ_SMS);
        permissions.add(Manifest.permission.RECEIVE_SMS);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        boolean needRequest = false;
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }

        if (needRequest) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS)) {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("SMS Permission Needed")
                    .setMessage("We need SMS access to automatically track your expenses and bills.")
                    .setPositiveButton("OK", (dialog, which) -> ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), SMS_PERMISSION_CODE))
                    .setNegativeButton("Cancel", null)
                    .create().show();
            } else {
                ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), SMS_PERMISSION_CODE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {
            syncHistoricalSms();
        } else {
            checkPendingSms();
        }
    }

    private void syncHistoricalSms() {
        android.content.SharedPreferences prefs = getSharedPreferences("ExpensaPrefs", MODE_PRIVATE);
        boolean done = prefs.getBoolean("historical_scan_done", false);
        if (done) return;

        new Thread(() -> {
            try {
                long ninetyDaysAgo = System.currentTimeMillis() - (90L * 24 * 60 * 60 * 1000);
                android.database.Cursor cursor = getContentResolver().query(
                        android.net.Uri.parse("content://sms/inbox"),
                        new String[]{"address", "date", "body"},
                        "date > ?",
                        new String[]{String.valueOf(ninetyDaysAgo)},
                        "date ASC"
                );

                if (cursor != null) {
                    com.smartexpense.mobile.database.AppDatabase db = com.smartexpense.mobile.database.AppDatabase.getDatabase(this);
                    while (cursor.moveToNext()) {
                        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
                        long date = cursor.getLong(cursor.getColumnIndexOrThrow("date"));
                        String sender = cursor.getString(cursor.getColumnIndexOrThrow("address"));

                        // Very basic check for bank
                        if (sender != null && sender.matches("(?i)(.*HDFCBK.*|.*SBIINB.*|.*ICICIB.*|.*AXISBK.*|.*KOTAKB.*|.*BOI.*|.*PNB.*)")) {
                            com.smartexpense.mobile.sms.SmsParser.ParsedData parsed = com.smartexpense.mobile.sms.SmsParser.parseMessage(body, date, body);
                            if (parsed != null) {
                                if (parsed.isBill()) {
                                    if (db.billDao().countByRawSms(parsed.bill.rawSms) == 0) {
                                        db.billDao().insert(parsed.bill);
                                    }
                                } else {
                                    if (db.transactionDao().countByRawSms(parsed.transaction.rawSms) == 0) {
                                        db.transactionDao().insert(parsed.transaction);
                                    }
                                }
                            }
                        }
                    }
                    cursor.close();
                    prefs.edit().putBoolean("historical_scan_done", true).apply();
                }
            } catch (Exception e) {}
            runOnUiThread(this::checkPendingSms);
        }).start();
    }

    private void checkPendingSms() {
        new Thread(() -> {
            com.smartexpense.mobile.database.AppDatabase db = com.smartexpense.mobile.database.AppDatabase.getDatabase(this);
            java.util.List<com.smartexpense.mobile.model.Transaction> pending = db.transactionDao().getPendingTransactionsSync();
            if (pending != null && !pending.isEmpty()) {
                runOnUiThread(() -> {
                    new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("Pending Transactions")
                        .setMessage(pending.size() + " new transactions were detected while you were away. Add them to expenses?")
                        .setPositiveButton("Show All", (dialog, which) -> {
                            // In real app, open a BottomSheet or Fragment. Here just mark them.
                            new Thread(() -> {
                                java.util.List<Integer> ids = new java.util.ArrayList<>();
                                for (com.smartexpense.mobile.model.Transaction t : pending) ids.add(t.id);
                                db.transactionDao().markAsReviewed(ids);
                            }).start();
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ExpensesFragment()).commit();
                        })
                        .setNegativeButton("Dismiss", null)
                        .create().show();
                });
            }
        }).start();
    }
}
