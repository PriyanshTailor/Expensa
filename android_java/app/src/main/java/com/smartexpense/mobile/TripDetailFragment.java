package com.smartexpense.mobile;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.smartexpense.mobile.model.Trip;

import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import java.util.Map;

public class TripDetailFragment extends Fragment {

    private String tripId = "";
    private String tripName = "Notebook";
    private TextView tvTripTotal;
    private TripMemberAdapter adapter;
    
    private double currentTotal = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trips, container, false);

        if (getArguments() != null) {
            tripId = getArguments().getString("TRIP_ID", tripId);
            tripName = getArguments().getString("TRIP_NAME", tripName);
        }

        TextView tvTitle = view.findViewById(R.id.tvTripTitle);
        if (tvTitle != null) {
            String title = "Notebook: " + tripName;
            tvTitle.setText(title);
        }

        tvTripTotal = view.findViewById(R.id.tvTripTotal);
        RecyclerView rvTripMembers = view.findViewById(R.id.rvTripMembers);
        
        rvTripMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TripMemberAdapter();
        rvTripMembers.setAdapter(adapter);

        
        initializeTripData();

        view.findViewById(R.id.btnAddTripExpense).setOnClickListener(v -> {
            AddTripExpenseBottomSheet sheet = new AddTripExpenseBottomSheet();
            Bundle args = new Bundle();
            args.putString("TRIP_ID", tripId);
            sheet.setArguments(args);
            sheet.setOnDismissListener(this::initializeTripData);
            sheet.show(getChildFragmentManager(), "add_trip_expense");
        });

        view.findViewById(R.id.btnShareTripPdf).setOnClickListener(v -> generateTripPdf());

        view.findViewById(R.id.btnAddMember).setOnClickListener(v -> showAddMemberDialog());

        return view;
    }

    private void showAddMemberDialog() {
        if (getContext() == null) return;
        android.widget.EditText et = new android.widget.EditText(getContext());
        et.setHint("Member Name");
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
            .setTitle("Add New Member")
            .setView(et)
            .setPositiveButton("Add", (dialog, which) -> {
                String name = et.getText().toString().trim();
                if (name.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter member name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (name.length() > 50) {
                    Toast.makeText(getContext(), "Member name too long (max 50 characters)", Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, String> data = new HashMap<>();
                data.put("name", name);
                com.smartexpense.mobile.network.RetrofitClient.getClient().create(com.smartexpense.mobile.network.ApiService.class).addTripMember(tripId, data)
                    .enqueue(new retrofit2.Callback<Trip>() {
                        @Override
                        public void onResponse(retrofit2.Call<Trip> call, retrofit2.Response<Trip> response) {
                            if (response.isSuccessful()) {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), name + " added successfully!", Toast.LENGTH_SHORT).show();
                                }
                                initializeTripData();
                            } else {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Failed to add member", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(retrofit2.Call<Trip> call, Throwable t) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Network Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void initializeTripData() {
        com.smartexpense.mobile.network.RetrofitClient.getClient().create(com.smartexpense.mobile.network.ApiService.class).getTrip(tripId)
            .enqueue(new retrofit2.Callback<Trip>() {
                @Override
                public void onResponse(retrofit2.Call<Trip> call, retrofit2.Response<Trip> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            Trip trip = response.body();
                            Map<String, Double> membersMap = trip.getMembers();
                            if (membersMap != null) {
                                List<TripDetailFragment.TripMember> members = new ArrayList<>();
                                double totalSpent = 0;
                                for (Map.Entry<String, Double> entry : membersMap.entrySet()) {
                                    double spent = entry.getValue() != null ? entry.getValue() : 0.0;
                                    members.add(new TripDetailFragment.TripMember(entry.getKey(), spent));
                                    totalSpent += spent;
                                }
                                currentTotal = totalSpent;
                                if (tvTripTotal != null) {
                                    tvTripTotal.setText(NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(totalSpent));
                                }
                                adapter.submitList(members);
                            } else {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "No members in this notebook", Toast.LENGTH_SHORT).show();
                                }
                                adapter.submitList(new ArrayList<>());
                            }
                        } catch (Exception e) {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Error parsing trip data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Notebook not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                
                @Override
                public void onFailure(retrofit2.Call<Trip> call, Throwable t) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void generateTripPdf() {
        if (adapter.getCurrentList().isEmpty()) {
            Toast.makeText(getContext(), "Add expenses first to generate receipt", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        int pageHeight = 300 + (adapter.getCurrentList().size() * 30);
        int pageWidth = 300;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        titlePaint.setTextSize(18);
        canvas.drawText("EXPENSA - NOTEBOOKS", pageWidth / 2f, 40, titlePaint);

        titlePaint.setTextSize(12);
        titlePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        String notebookTitle = tripName != null ? tripName : "Notebook";
        canvas.drawText(notebookTitle.toUpperCase() + " SETTLEMENT", pageWidth / 2f, 60, titlePaint);
        canvas.drawText("Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()), pageWidth / 2f, 80, titlePaint);

        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        paint.setTextSize(10);
        paint.setColor(Color.BLACK);

        canvas.drawLine(20, 100, pageWidth - 20, 100, paint);
        
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("MEMBER", 20, 120, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("TOTAL SPENT", pageWidth - 20, 120, paint);

        canvas.drawLine(20, 130, pageWidth - 20, 130, paint);

        int y = 150;
        for (TripDetailFragment.TripMember ex : adapter.getCurrentList()) {
            paint.setTextAlign(Paint.Align.LEFT);
            String name = ex.name;
            if (name == null) name = "Unknown";
            if (name.length() > 15) name = name.substring(0, 12) + "...";
            canvas.drawText(name, 20, y, paint);
            
            paint.setTextAlign(Paint.Align.RIGHT);
            try {
                String spent = NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(ex.spent);
                canvas.drawText(spent, pageWidth - 20, y, paint);
            } catch (Exception e) {
                canvas.drawText("₹" + String.format("%.2f", ex.spent), pageWidth - 20, y, paint);
            }

            y += 20;
        }

        canvas.drawLine(20, y + 10, pageWidth - 20, y + 10, paint);
        
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTextSize(14);
        titlePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("TOTAL COST:", 20, y + 40, titlePaint);

        titlePaint.setTextAlign(Paint.Align.RIGHT);
        try {
            String total = NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(currentTotal);
            canvas.drawText(total, pageWidth - 20, y + 40, titlePaint);
        } catch (Exception e) {
            canvas.drawText("₹" + String.format("%.2f", currentTotal), pageWidth - 20, y + 40, titlePaint);
        }

        pdfDocument.finishPage(page);

        try {
            if (getContext() == null) {
                return;
            }

            File pdfFolder = new File(getContext().getCacheDir(), "pdfs");
            if (!pdfFolder.exists() && !pdfFolder.mkdirs()) {
                Toast.makeText(getContext(), "Failed to create PDF folder", Toast.LENGTH_SHORT).show();
                return;
            }

            String fileName = "Expensa_" + (notebookTitle.replace(" ", "_")).replace("/", "_") + "_" + System.currentTimeMillis() + ".pdf";
            File file = new File(pdfFolder, fileName);
            pdfDocument.writeTo(new FileOutputStream(file));
            
            Uri pdfUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);
            
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, notebookTitle + " - Settlement Report");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Please find attached the notebook settlement report.");
            shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(android.content.Intent.createChooser(shareIntent, "Share PDF via"));
            Toast.makeText(getContext(), "PDF generated successfully!", Toast.LENGTH_SHORT).show();

        } catch (java.io.IOException e) {
            android.util.Log.e("TripDetail", "IO Error generating PDF: " + e.getMessage(), e);
            Toast.makeText(getContext(), "File write error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            android.util.Log.e("TripDetail", "Error generating PDF: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pdfDocument.close();
        }
    }

    public static class TripMember {
        public String name;
        public double spent;
        public TripMember(String name, double spent) {
            this.name = name;
            this.spent = spent;
        }
    }
}
