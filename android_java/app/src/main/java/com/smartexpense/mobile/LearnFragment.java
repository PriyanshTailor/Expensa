package com.smartexpense.mobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

public class LearnFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_learn, container, false);

        ImageView btnPlay = view.findViewById(R.id.btnPlayVideo);
        CardView cardBudgeting = view.findViewById(R.id.cardBudgeting);
        CardView cardSIP = view.findViewById(R.id.cardSIP);
        CardView cardTax = view.findViewById(R.id.cardTax);

        btnPlay.setOnClickListener(v -> openYoutube("cosjtcWyWlI")); // Power of compounding
        cardBudgeting.setOnClickListener(v -> openUrl("https://www.nerdwallet.com/article/finance/how-to-budget"));
        cardSIP.setOnClickListener(v -> openUrl("https://groww.in/p/systematic-investment-plan"));
        cardTax.setOnClickListener(v -> openUrl("https://cleartax.in/s/80c-80-deductions"));

        return view;
    }

    private void openYoutube(String videoId) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoId));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + videoId));
        try {
            startActivity(appIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            startActivity(webIntent);
        }
    }

    private void openUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}
