package com.example.kimlongflower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewTransaction extends AppCompatActivity {

    private TextView tvStartDateInTransaction;
    private TextView tvEndDateInTransaction;
    private DatePickerDialog.OnDateSetListener startDateSetListener;
    private DatePickerDialog.OnDateSetListener endDateSetListener;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    TextView tvFunds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction);

        Date currentDate = new Date();
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
        tvStartDateInTransaction = findViewById(R.id.tvStartDateInTransaction);
        tvEndDateInTransaction = findViewById(R.id.tvEndDateInTransaction);
        tvStartDateInTransaction.setText(formatterDate.format(currentDate));
        tvEndDateInTransaction.setText(formatterDate.format(currentDate));

        //Pick start date
        Button bntPickStartDateInTransaction = findViewById(R.id.bntPickStartDateInTransaction);
        bntPickStartDateInTransaction.setOnClickListener(v->{
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int date = calendar.get(Calendar.DATE);

            DatePickerDialog dialog = new DatePickerDialog(
                    ViewTransaction.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    startDateSetListener,year,month,date);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        //set start date
        startDateSetListener = (view, year, month, dayOfMonth) -> {
            month +=1;
            String startDate = dayOfMonth+"/"+month+"/"+year;
            tvEndDateInTransaction = findViewById(R.id.tvEndDateInTransaction);
            String endDate = tvEndDateInTransaction.getText().toString();
            tvStartDateInTransaction.setText(startDate);
            if(!ViewHistoryOfInvoices.isSearchDateValid(startDate,endDate)) {
                tvEndDateInTransaction.setText(startDate);
            }
        };

        //Pick end date
        Button bntPickEndDateInTransaction = findViewById(R.id.bntPickEndDateInTransaction);
        bntPickEndDateInTransaction.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int date = calendar.get(Calendar.DATE);

            DatePickerDialog dialog = new DatePickerDialog(
                    ViewTransaction.this,
                    android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                    endDateSetListener,year,month,date);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        //set end date
        endDateSetListener = (view, year, month, dayOfMonth) -> {
            month +=1;
            String endDate = dayOfMonth+"/"+month+"/"+year;
            tvStartDateInTransaction = findViewById(R.id.tvStartDateInTransaction);
            String startDate = tvStartDateInTransaction.getText().toString();
            tvEndDateInTransaction.setText(endDate);
            if(!ViewHistoryOfInvoices.isSearchDateValid(startDate,endDate)) {
                tvStartDateInTransaction.setText(endDate);
            }
        };

        //Show funds
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users").child(userId);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.hasChild("funds")){
                    databaseReference.child("funds").setValue("0");
                }
                databaseReference.child("funds").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvFunds = findViewById(R.id.tvFunds);
                        tvFunds.setText(snapshot.getValue(String.class));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //add funds
        Button bntAddFunds = findViewById(R.id.bntAddFunds);
        bntAddFunds.setOnClickListener(v->{
            tvFunds = findViewById(R.id.tvFunds);
            String oldValue = tvFunds.getText().toString();
            databaseReference.child("funds").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String newValue = snapshot.getValue(String.class);
                    databaseReference.child("funds").setValue(Integer.parseInt(oldValue)+Integer.parseInt(newValue));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        });



        Button bntBackMainPageFromViewTransaction = findViewById(R.id.bntBackMainPageFromViewTransaction);
        bntBackMainPageFromViewTransaction.setOnClickListener(v->{finish();});

        ImageButton ibBackMainPageFromViewTransaction = findViewById(R.id.ibBackMainPageFromViewTransaction);
        ibBackMainPageFromViewTransaction.setOnClickListener(v->{finish();});
    }
}