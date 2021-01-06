package com.example.kimlongflower;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;

public class ChooseTypeOfHistory extends AppCompatActivity {

    Button btnDirectToSellHistory;
    Button bntDirectToBuyHistory;
    ImageButton ibBackMainPage;
    Button bntBackMainPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_type_history);

        bntDirectToBuyHistory = findViewById(R.id.bntDirectToBuyHistory);
        bntDirectToBuyHistory.setOnClickListener(v -> startActivity(new Intent(ChooseTypeOfHistory.this,ViewHistoryOfInvoices.class).putExtra("action","buy")));

        btnDirectToSellHistory = findViewById(R.id.bntDirectToSellHistory);
        btnDirectToSellHistory.setOnClickListener(v -> startActivity(new Intent(ChooseTypeOfHistory.this,ViewHistoryOfInvoices.class).putExtra("action","sell")));

        bntBackMainPage = findViewById(R.id.bntBackMainPage);
        bntBackMainPage.setOnClickListener(v -> finish());

        ibBackMainPage = findViewById(R.id.ibBackMainPage);
        ibBackMainPage.setOnClickListener(v -> finish());
    }
}