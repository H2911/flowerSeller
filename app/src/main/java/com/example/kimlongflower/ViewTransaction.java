package com.example.kimlongflower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

    Dialog dialogTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction);

        dialogTransaction = new Dialog(this);

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

        //get funds from database
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("Users").child(userId).child("funds");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                databaseReference.child("value").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        tvFunds = findViewById(R.id.tvFunds);
                        tvFunds.setText(snapshot.getValue(String.class)+" đ");
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
            dialogTransaction.setContentView(R.layout.activity_add_funds);

            EditText edPerformer;
            EditText edFundsAdd;
            Button btnConfirmAddFunds;
            Button btnCancelAddFunds;

            edPerformer = dialogTransaction.findViewById(R.id.edPerformer);
            edFundsAdd = dialogTransaction.findViewById(R.id.edFundsAdd);
            btnConfirmAddFunds = dialogTransaction.findViewById(R.id.btnConfirmAddFunds);
            btnCancelAddFunds = dialogTransaction.findViewById(R.id.btnCancelAddFunds);

            btnCancelAddFunds.setOnClickListener(v2->{
                dialogTransaction.dismiss();
            });

            btnConfirmAddFunds.setOnClickListener(v1->{
                if(edFundsAdd.getText().toString().trim().equals("")){
                    Toast.makeText(ViewTransaction.this,"Số vốn thêm không được bỏ trống!",Toast.LENGTH_SHORT).show();
                }else if(edPerformer.getText().toString().trim().equals("")){
                    Toast.makeText(ViewTransaction.this,"Người thực hiện không được bỏ trống!",Toast.LENGTH_SHORT).show();
                }else{
                    final AlertDialog.Builder alertConfirm = new AlertDialog.Builder(ViewTransaction.this);
                    alertConfirm.setMessage("Xác nhận thêm vốn!");
                    alertConfirm.setPositiveButton("Xác nhận", (dialog, which) -> {
                        HashMap<String,Object> updateData = new HashMap<>();

                        //Change value
                        databaseReference.child("value").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String oldValue =  snapshot.getValue(String.class);
                                String newValue = edFundsAdd.getText().toString();

                                String changedValue = String.valueOf(Integer.parseInt(oldValue)+Integer.parseInt(newValue));
                                //Add value to database
                                updateData.put("value", changedValue);
                                databaseReference.updateChildren(updateData).addOnCompleteListener(task -> {
                                   final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ViewTransaction.this);
                                   alertDialog.setMessage("Đã thêm "+ newValue +" vào vốn"+"\n"+"Vốn hiện tại: "+changedValue);
                                   alertDialog.setPositiveButton("OK",(dialog, which) -> {
                                   });
                                });

                                //Save history
                                databaseReference.child("selfAdd").child(getCurrentDate()).child(getCurrentTime()).setValue(edPerformer.getText().toString(),edFundsAdd.getText().toString());
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        dialogTransaction.dismiss();
                    });
                    alertConfirm.setNegativeButton("Hủy", (dialog, which) -> {
                    });
                    alertConfirm.create().show();
                }
            });
            dialogTransaction.show();
        });



        Button bntBackMainPageFromViewTransaction = findViewById(R.id.bntBackMainPageFromViewTransaction);
        bntBackMainPageFromViewTransaction.setOnClickListener(v->{finish();});

        ImageButton ibBackMainPageFromViewTransaction = findViewById(R.id.ibBackMainPageFromViewTransaction);
        ibBackMainPageFromViewTransaction.setOnClickListener(v->{finish();});
    }

    public String getCurrentTime(){
        Date currentDate = new Date();
        SimpleDateFormat formatterTime = new SimpleDateFormat("hh:mm:ss");
        return formatterTime.format(currentDate);
    }

    public String getCurrentDate(){
        Date currentDate = new Date();
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd_MM_yyyy");
        return formatterDate.format(currentDate);
    }
}