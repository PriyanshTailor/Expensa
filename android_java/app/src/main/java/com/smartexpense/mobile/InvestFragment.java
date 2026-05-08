package com.smartexpense.mobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InvestFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invest, container, false);

        Button btnMF = view.findViewById(R.id.btnInvestMF);
        Button btnFD = view.findViewById(R.id.btnInvestFD);
        Button btnPost = view.findViewById(R.id.btnInvestPost);

        btnMF.setOnClickListener(v -> openUrl("https://groww.in/mutual-funds"));
        btnFD.setOnClickListener(v -> openUrl("https://sbi.co.in/web/personal-banking/investments-deposits/deposits"));
        btnPost.setOnClickListener(v -> openUrl("https://www.indiapost.gov.in/Financial/pages/content/post-office-saving-schemes.aspx"));

        return view;
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
