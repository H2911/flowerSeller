package com.example.kimlongflower;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity {

    //List contain products
    public static List<Item> productsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Buy product
        Button bntBuy = findViewById(R.id.bntBuy);
        bntBuy.setOnClickListener(v -> startActivity(new Intent(Main.this, CreateInvoice.class).putExtra("action","buy")));

        //Sell product
        Button bntSell = findViewById(R.id.bntSell);
        bntSell.setOnClickListener(v -> startActivity(new Intent(Main.this, CreateInvoice.class).putExtra("action","sell")));

        //View store
        Button bntViewStore = findViewById(R.id.bntViewStore);
        bntViewStore.setOnClickListener(v -> startActivity(new Intent(Main.this, ListProduct.class).putExtra("action","view store")));

        //View transaction
        Button bntViewSummary = findViewById(R.id.bntViewTransaction);
        bntViewSummary.setOnClickListener(v -> startActivity( new Intent(Main.this, ViewTransaction.class)));

        //View history of invoices
        Button bntViewHistory = findViewById(R.id.bntViewHistoryOfInvoice);
        bntViewHistory.setOnClickListener(v -> startActivity(new Intent(Main.this, ChooseTypeOfHistory.class)));

        //Logout
        Button bntLogout = findViewById(R.id.bntLogout);
        bntLogout.setOnClickListener(v -> logOut());
    }

    public void onBackPressed(){
        logOut();
    }

    private void logOut(){
        final AlertDialog.Builder quitAlert = new AlertDialog.Builder(Main.this);
        quitAlert.setMessage("Đăng xuất khỏi tài khoản?");
        quitAlert.setPositiveButton("Có", (dialog, which) -> finish());
        quitAlert.setNegativeButton("Không", (dialog, which) -> {
        });
        quitAlert.create().show();
    }
}