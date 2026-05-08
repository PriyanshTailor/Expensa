package com.smartexpense.mobile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.lifecycle.ViewModelProvider;
import com.smartexpense.mobile.model.Transaction;
import com.smartexpense.mobile.viewmodel.ExpenseViewModel;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ExpensesFragment extends Fragment {

    private TextView tvTotalSpent, tvTotalIncome, tvNetSavings;
    private RecyclerView rvExpenses;
    private FloatingActionButton fabAdd;
    private ExpenseViewModel expenseViewModel;
    private ExpenseAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expenses, container, false);

        initViews(view);

        expenseViewModel = new ViewModelProvider(this).get(ExpenseViewModel.class);
        expenseViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.submitList(transactions);
            calculateTotals(transactions);
        });

        fabAdd.setOnClickListener(v -> {
            new AddExpenseBottomSheet().show(getChildFragmentManager(), "add_expense");
        });

        return view;
    }

    private void initViews(View view) {
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome);
        tvNetSavings = view.findViewById(R.id.tvNetSavings);
        rvExpenses = view.findViewById(R.id.rvExpenses);
        fabAdd = view.findViewById(R.id.fabAddExpense);
        
        View btnExport = view.findViewById(R.id.btnExportReport);
        if (btnExport != null) btnExport.setOnClickListener(v -> generatePdfReceipt());

        rvExpenses.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ExpenseAdapter();
        rvExpenses.setAdapter(adapter);
    }

    private void calculateTotals(java.util.List<Transaction> transactions) {
        double spent = 0;
        double income = 0;
        
        for (Transaction t : transactions) {
            if ("debit".equalsIgnoreCase(t.type)) {
                spent += t.amount;
            } else {
                income += t.amount;
            }
        }
        
        tvTotalSpent.setText(formatCurrency(spent));
        tvTotalIncome.setText(formatCurrency(income));
        tvNetSavings.setText(formatCurrency(income - spent));
    }

    private void generatePdfReceipt() {
        if (adapter.getCurrentList().isEmpty()) {
            Toast.makeText(getContext(), "No expenses to generate receipt", Toast.LENGTH_SHORT).show();
            return;
        }

        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        Paint titlePaint = new Paint();

        // Standard receipt width: roughly 300px, length depends on items
        int pageHeight = 300 + (adapter.getCurrentList().size() * 30);
        int pageWidth = 300;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // Draw background
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);

        // Header
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        titlePaint.setTextSize(18);
        canvas.drawText("EXPENSA", pageWidth / 2f, 40, titlePaint);
        
        titlePaint.setTextSize(12);
        titlePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        canvas.drawText("TAX INVOICE / RECEIPT", pageWidth / 2f, 60, titlePaint);
        canvas.drawText("Date: " + new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date()), pageWidth / 2f, 80, titlePaint);

        paint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        paint.setTextSize(10);
        paint.setColor(Color.BLACK);

        canvas.drawLine(20, 100, pageWidth - 20, 100, paint);
        
        // Columns
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("ITEM", 20, 120, paint);
        canvas.drawText("TYPE", 150, 120, paint);
        paint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText("AMOUNT", pageWidth - 20, 120, paint);

        canvas.drawLine(20, 130, pageWidth - 20, 130, paint);

        int y = 150;
        double totalSpent = 0;
        
        for (Transaction ex : adapter.getCurrentList()) {
            paint.setTextAlign(Paint.Align.LEFT);
            String merchant = ex.merchant;
            if (merchant.length() > 15) merchant = merchant.substring(0, 12) + "...";
            canvas.drawText(merchant, 20, y, paint);
            
            canvas.drawText(ex.type.toUpperCase(), 150, y, paint);
            
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(formatCurrency(ex.amount), pageWidth - 20, y, paint);
            
            if ("DEBIT".equalsIgnoreCase(ex.type)) totalSpent += ex.amount;
            
            y += 20;
        }

        canvas.drawLine(20, y + 10, pageWidth - 20, y + 10, paint);
        
        // Total
        titlePaint.setTextAlign(Paint.Align.LEFT);
        titlePaint.setTextSize(14);
        titlePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
        canvas.drawText("TOTAL SPENT:", 20, y + 40, titlePaint);
        
        titlePaint.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(formatCurrency(totalSpent), pageWidth - 20, y + 40, titlePaint);

        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(10);
        titlePaint.setTypeface(Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL));
        canvas.drawText("Thank you for using Expensa!", pageWidth / 2f, y + 80, titlePaint);

        pdfDocument.finishPage(page);

        try {
            File pdfFolder = new File(getContext().getCacheDir(), "pdfs");
            if (!pdfFolder.exists()) pdfFolder.mkdirs();
            
            String fileName = "Expensa_Receipt_" + System.currentTimeMillis() + ".pdf";
            File file = new File(pdfFolder, fileName);
            pdfDocument.writeTo(new FileOutputStream(file));
            
            Uri pdfUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", file);
            
            android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(android.content.Intent.EXTRA_STREAM, pdfUri);
            shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Expensa Receipt");
            shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Here is my latest expense receipt generated via Expensa.");
            shareIntent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            startActivity(android.content.Intent.createChooser(shareIntent, "Share Receipt via"));
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Failed to generate PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            pdfDocument.close();
        }
    }

    private String formatCurrency(double amount) {
        return NumberFormat.getCurrencyInstance(new Locale("en", "IN")).format(amount);
    }
}
