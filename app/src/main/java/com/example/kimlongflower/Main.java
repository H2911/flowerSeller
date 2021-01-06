package com.example.kimlongflower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class Main extends AppCompatActivity {

    //List contain products
    public static List<Item> productsList = new ArrayList<>();

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkFunds();

        //Buy product
        Button btnBuy = findViewById(R.id.bntBuy);
        btnBuy.setOnClickListener(v -> startActivity(new Intent(Main.this, CreateInvoice.class).putExtra("action","buy")));

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

    private void checkFunds(){
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();

        //get funds from database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users").child(userId).child("funds");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //First time view transaction
                if(!snapshot.hasChild("value")){
                    databaseReference.child("value").setValue("0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}