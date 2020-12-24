package com.example.kimlongflower;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

public class SeparateHistory extends AppCompatActivity {

    Button bntDirectToSellHistory;
    Button bntDirectToBuyHistory;
    ImageButton ibBackMainPage;
    Button bntBackMainPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_separate_history);

        bntDirectToBuyHistory = findViewById(R.id.bntDirectToBuyHistory);
        bntDirectToBuyHistory.setOnClickListener(v -> {
            startActivity(new Intent(SeparateHistory.this,ViewHistoryOfInvoices.class).putExtra("action","buy"));
        });

        bntDirectToSellHistory = findViewById(R.id.bntDirectToSellHistory);
        bntDirectToSellHistory.setOnClickListener(v -> {
            startActivity(new Intent(SeparateHistory.this,ViewHistoryOfInvoices.class).putExtra("action","sell"));
        });

        bntBackMainPage = findViewById(R.id.bntBackMainPage);
        bntBackMainPage.setOnClickListener(v -> {
            finish();
        });

        ibBackMainPage = findViewById(R.id.ibBackMainPage);
        ibBackMainPage.setOnClickListener(v -> {
            finish();
        });
    }
}