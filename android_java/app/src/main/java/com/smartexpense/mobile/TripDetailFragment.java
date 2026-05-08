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
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.File;
import java.io.FileOutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TripDetailFragment extends Fragment {

    private String tripId = "";
    private String tripName = "Notebook";
    private TextView tvTripTotal;
    private RecyclerView rvTripMembers;
    private TripMemberAdapter adapter;
    private FirebaseFirestore db;
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
        if (tvTitle != null) tvTitle.setText("Notebook: " + tripName);

        tvTripTotal = view.findViewById(R.id.tvTripTotal);
        rvTripMembers = view.findViewById(R.id.rvTripMembers);
        
        rvTripMembers.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TripMemberAdapter();
        rvTripMembers.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        initializeTripData();

        view.findViewById(R.id.btnAddTripExpense).setOnClickListener(v -> {
            AddTripExpenseBottomSheet sheet = new AddTripExpenseBottomSheet();
            Bundle args = new Bundle();
            args.putString("TRIP_ID", tripId);
            sheet.setArguments(args);
            sheet.show(getChildFragmentManager(), "add_trip_expense");
        });

        view.findViewById(R.id.btnShareTripPdf).setOnClickListener(v -> generateTripPdf());

        view.findViewById(R.id.btnAddMember).setOnClickListener(v -> showAddMemberDialog());

        return view;
    }

    private void showAddMemberDialog() {
        android.widget.EditText et = new android.widget.EditText(getContext());
        et.setHint("Member Name");
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
            .setTitle("Add New Member")
            .setView(et)
            .setPositiveButton("Add", (dialog, which) -> {
                String name = et.getText().toString().trim();
                if (!name.isEmpty()) {
                    db.collection("trips").document(tripId)
                        .update("members." + name, 0.0)
                        .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), name + " added!", Toast.LENGTH_SHORT).show());
                }
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void initializeTripData() {
        db.collection("trips").document(tripId).addSnapshotListener((doc, e) -> {
            if (e != null) {
                if (getContext() != null) Toast.makeText(getContext(), "Error loading trip", Toast.LENGTH_SHORT).show();
                return;
            }

            if (doc != null && doc.exists()) {
                Map<String, Object> membersMap = (Map<String, Object>) doc.get("members");
                if (membersMap != null) {
                    List<TripDetailFragment.TripMember> members = new ArrayList<>();
                    double totalSpent = 0;
                    for (Map.Entry<String, Object> entry : membersMap.entrySet()) {
                        double spent = 0;
                        if (entry.getValue() instanceof Number) {
                            spent = ((Number) entry.getValue()).doubleValue();
                        }
                        members.add(new TripDetailFragment.TripMember(entry.getKey(), spent));
                        totalSpent += spent;
                    }
                    currentTotal = totalSpent;
                    tvTripTotal.setText(NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(totalSpent));
                    adapter.submitList(members);
                }
            }
        });
    }

    private void generateTripPdf() {
        if (adapter.getCurrentList().isEmpty()) {
            Toast.makeText(getContext(), "No expenses to generate receipt", Toast.LENGTH_SHORT).show();
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
        canvas.drawText("EXPENSA - TRIPS", pageWidth / 2f, 40, titlePaint);
        
        titlePaint.setTextSize(12);
        titlePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        canvas.drawText(tripName.toUpperCase() + " SETTLEMENT", pageWidth / 2f, 60, titlePaint);
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
            if (name.length() > 15) name = name.substring(0, 12) + "...";
            canvas.drawText(name, 20, y, paint);
            
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(ex.spent), pageWidth - 20, y, paint);
            
            y += 20;
        }

        canvas.drawLine(20, y + 10, pageWidth - 20, y + 10, paint);
        
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTextSize(14);
        titlePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("TOTAL TRIP COST:", 20, y + 40, titlePaint);
        
        titlePaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(currentTotal), pageWidth - 20, y + 40, titlePaint);

        pdfDocument.finishPage(page);

        try {
            File pdfFolder = new File(getContext().getCacheDir(), "pdfs");
            if (!pdfFolder.exists()) pdfFolder.mkdirs();
            
            String fileName = "Expensa_" + tripName.replace(" ", "_") + ".pdf";
            File file = new File(pdfFolder, fileName);
            pdfDocument.writeTo(new FileOutputStream(file));
            
            Uri pdfUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);
            
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, tripName + " Settlement");
            shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(android.content.Intent.createChooser(shareIntent, "Share via WhatsApp"));
            
        } catch (Exception e) {
            e.printStackTrace();
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
