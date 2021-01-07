package com.example.kimlongflower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.internal.bind.ReflectiveTypeAdapterFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
    GridView gvViewTransaction;
    CustomAdapterTransaction customAdapterTransaction;

    Dialog dialogAddFundsTransaction;
    Dialog dialogViewDetailTransaction;

    List<Transaction> listTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transaction);

        dialogAddFundsTransaction = new Dialog(this);
        dialogViewDetailTransaction = new Dialog(this);

        Date currentDate = new Date();
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");

        tvStartDateInTransaction = findViewById(R.id.tvStartDateInTransaction);
        tvEndDateInTransaction = findViewById(R.id.tvEndDateInTransaction);
        tvStartDateInTransaction.setText(formatterDate.format(currentDate));
        tvEndDateInTransaction.setText(formatterDate.format(currentDate));

        //Pick start date
        Button btnPickStartDateInTransaction = findViewById(R.id.btnPickStartDateInTransaction);
        btnPickStartDateInTransaction.setOnClickListener(v->{
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
        Button bntPickEndDateInTransaction = findViewById(R.id.btnPickEndDateInTransaction);
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

        //Search history of Transaction
        Button btnSearchTransaction = findViewById(R.id.btnSearchTransaction);
        btnSearchTransaction.setOnClickListener(v -> {
            listTransaction = new ArrayList<>();

            firebaseAuth = FirebaseAuth.getInstance();
            firebaseUser = firebaseAuth.getCurrentUser();
            String userId = firebaseUser.getUid();

            tvStartDateInTransaction = findViewById(R.id.tvStartDateInTransaction);
            tvEndDateInTransaction = findViewById(R.id.tvEndDateInTransaction);

            firebaseDatabase = FirebaseDatabase.getInstance();

            //get history of buy
            databaseReference = firebaseDatabase.getReference("Users").child(userId).child("funds").child("buy");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = "";
                    String changeValue = "";
                    //get date from database
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        //check query
                        if(ViewHistoryOfInvoices.isDateBetweenStartDateAndEndDate(tvStartDateInTransaction.getText().toString(),tvEndDateInTransaction.getText().toString(),dataSnapshot.getKey())){
                            //get time data from selected date
                            for(DataSnapshot timeData:dataSnapshot.getChildren()){
                                //get seller or buyer name
                                for(DataSnapshot nameData:timeData.getChildren()){
                                    name = nameData.getKey();
                                    changeValue = String.valueOf(nameData.getValue(Long.class));
                                    listTransaction.add(new Transaction(dataSnapshot.getKey(),timeData.getKey(),name,"buy",changeValue));
                                }
                            }
                        }
                    }
                    customAdapterTransaction.notifyDataSetChanged();
                    if(listTransaction.isEmpty()){
                        Toast.makeText(ViewTransaction.this,"Không có giao dịch bán hàng!",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            //get history of sell
            databaseReference = firebaseDatabase.getReference("Users").child(userId).child("funds").child("sell");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = "";
                    String changeValue = "";
                    //get date from database
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        //check query
                        if(ViewHistoryOfInvoices.isDateBetweenStartDateAndEndDate(tvStartDateInTransaction.getText().toString(),tvEndDateInTransaction.getText().toString(),dataSnapshot.getKey())){
                            //get time data from selected date
                            for(DataSnapshot timeData:dataSnapshot.getChildren()){
                                //get seller or buyer name
                                for(DataSnapshot nameData:timeData.getChildren()){
                                    name = nameData.getKey();
                                    changeValue = String.valueOf(nameData.getValue(Long.class));
                                    listTransaction.add(new Transaction(dataSnapshot.getKey(),timeData.getKey(),name,"sell",changeValue));
                                }
                            }
                        }
                    }
                    customAdapterTransaction.notifyDataSetChanged();
                    if(listTransaction.isEmpty()){
                        Toast.makeText(ViewTransaction.this,"Không có giao dịch mua hàng!",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            //get history of self add
            databaseReference = firebaseDatabase.getReference("Users").child(userId).child("funds").child("selfAdd");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name;
                    String changeValue;
                    //get date from database
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        //check query
                        if(ViewHistoryOfInvoices.isDateBetweenStartDateAndEndDate(tvStartDateInTransaction.getText().toString(),tvEndDateInTransaction.getText().toString(),dataSnapshot.getKey())){
                            //get time data from selected date
                            for(DataSnapshot timeData:dataSnapshot.getChildren()){
                                //get seller or buyer name
                                for(DataSnapshot nameData:timeData.getChildren()){
                                    name = nameData.getKey();
                                    changeValue = String.valueOf(nameData.getValue(String.class));
                                    listTransaction.add(new Transaction(dataSnapshot.getKey(),timeData.getKey(),name,"selfAdd",changeValue));

                                }
                            }
                        }
                    }
                    customAdapterTransaction.notifyDataSetChanged();
                    if(listTransaction.isEmpty()){
                        Toast.makeText(ViewTransaction.this,"Không có lịch sử tự thêm vốn!",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            //Show transaction in grid view
            gvViewTransaction = findViewById(R.id.gvTransaction);
            customAdapterTransaction = new CustomAdapterTransaction(this,listTransaction);
            gvViewTransaction.setAdapter(customAdapterTransaction);
        });

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
                        tvFunds.setText(NumberTextWatcherForThousand.getDecimalFormattedString(String.valueOf(snapshot.getValue(Long.class)))+" đ");
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
        Button bntAddFunds = findViewById(R.id.btnAddFunds);
        bntAddFunds.setOnClickListener(v->{

            tvFunds = findViewById(R.id.tvFunds);
            dialogAddFundsTransaction.setContentView(R.layout.activity_add_funds);

            EditText edPerformer;
            EditText edFundsAdd;
            Button btnConfirmAddFunds;
            Button btnCancelAddFunds;

            edPerformer = dialogAddFundsTransaction.findViewById(R.id.edPerformer);
            edFundsAdd = dialogAddFundsTransaction.findViewById(R.id.edFundsAdd);
            //separate number by thousand
            edFundsAdd.addTextChangedListener(new NumberTextWatcherForThousand(edFundsAdd));

            btnConfirmAddFunds = dialogAddFundsTransaction.findViewById(R.id.btnConfirmAddFunds);
            btnCancelAddFunds = dialogAddFundsTransaction.findViewById(R.id.btnCancelAddFunds);

            btnCancelAddFunds.setOnClickListener(v2->{
                dialogAddFundsTransaction.dismiss();
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
                                String oldValue = String.valueOf(snapshot.getValue(Long.class));
                                String newValue = NumberTextWatcherForThousand.trimCommaOfString(edFundsAdd.getText().toString());

                                String changedValue = String.valueOf(Long.parseLong(oldValue)+Long.parseLong(newValue));
                                //Add value to database
                                updateData.put("value", Long.parseLong(changedValue));
                                databaseReference.updateChildren(updateData).addOnCompleteListener(task -> {
                                    if(task.isSuccessful()) {
                                        //Save history
                                        DatabaseReference referenceOfFundsHistory = databaseReference.child("selfAdd").child(getCurrentDate()).child(getCurrentTime()).child(edPerformer.getText().toString());
                                        referenceOfFundsHistory.setValue(NumberTextWatcherForThousand.trimCommaOfString(edFundsAdd.getText().toString())).addOnCompleteListener(task1 -> {
                                            if (task1.isSuccessful()) {
                                                final AlertDialog.Builder alertAddSuccessful = new AlertDialog.Builder(ViewTransaction.this);
                                                alertAddSuccessful.setMessage("Đã thêm " + NumberTextWatcherForThousand.getDecimalFormattedString(newValue) +
                                                        " vào vốn" + "\n" + "Vốn hiện tại: " + NumberTextWatcherForThousand.getDecimalFormattedString(changedValue));
                                                alertAddSuccessful.setPositiveButton("OK", (dialog, which) -> {
                                                });
                                                alertAddSuccessful.show();
                                            }
                                            else {
                                                Toast.makeText(ViewTransaction.this,"Lưu lịch sử vốn thất bại",Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                    else {
                                        Toast.makeText(ViewTransaction.this,"Cập nhật vốn thất bại",Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        dialogAddFundsTransaction.dismiss();
                    });
                    alertConfirm.setNegativeButton("Hủy", (dialog, which) -> {
                    });
                    alertConfirm.create().show();
                }
            });
            dialogAddFundsTransaction.show();
        });



        Button bntBackMainPageFromViewTransaction = findViewById(R.id.btnBackMainPageFromViewTransaction);
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

    public class CustomAdapterTransaction extends BaseAdapter{
        private Context context;
        private List<Transaction> listTransaction;

        public CustomAdapterTransaction(Context context, List<Transaction> listTransaction) {
            this.context = context;
            this.listTransaction = listTransaction;
        }

        @Override
        public int getCount() {
            return listTransaction.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = getLayoutInflater().inflate(R.layout.row_transaction,null);
            TextView date;
            TextView time;
            TextView name;
            TextView changeValue;
            Button btnViewDetailTransaction;

            date = v.findViewById(R.id.tvDateTransaction);
            time = v.findViewById(R.id.tvTimeTransaction);
            name = v.findViewById(R.id.tvNameTransaction);
            changeValue = v.findViewById(R.id.tvChangeTransaction);
            btnViewDetailTransaction = v.findViewById(R.id.bntViewDetailOfTransaction);

            date.setText(listTransaction.get(position).getDate());
            time.setText(listTransaction.get(position).getTime());
            name.setText(listTransaction.get(position).getPerformer());
            if(listTransaction.get(position).getAction().equals("buy")) {
                changeValue.setText("- "+NumberTextWatcherForThousand.getDecimalFormattedString(listTransaction.get(position).getChangeValue()));
            }else if (listTransaction.get(position).getAction().equals("sell")){
                changeValue.setText("+ "+NumberTextWatcherForThousand.getDecimalFormattedString(listTransaction.get(position).getChangeValue()));
            }
            else{
                changeValue.setText("+ "+NumberTextWatcherForThousand.getDecimalFormattedString(listTransaction.get(position).getChangeValue()));
            }
            btnViewDetailTransaction.setOnClickListener(v1 -> {
                dialogViewDetailTransaction.setContentView(R.layout.activity_popup_transaction);

                TextView tvDateTransactionInPopUp = dialogViewDetailTransaction.findViewById(R.id.tvDateTransactionInPopUp);
                TextView tvTimeTransactionInPopUp = dialogViewDetailTransaction.findViewById(R.id.tvTimeTransactionInPopUp);
                TextView tvActionTransaction = dialogViewDetailTransaction.findViewById(R.id.tvActionTransaction);
                TextView tvNameInTransactionInPopUp = dialogViewDetailTransaction.findViewById(R.id.tvNameInTransactionInPopUp);
                TextView tvChangeValueTransactionInPopUp = dialogViewDetailTransaction.findViewById(R.id.tvChangeValueTransactionInPopUp);
                Button btnCancelPopupViewDetailTransaction = dialogViewDetailTransaction.findViewById(R.id.btnCancelPopupViewDetailTransaction);

                tvDateTransactionInPopUp.setText("Ngày: "+listTransaction.get(position).getDate());
                tvTimeTransactionInPopUp.setText("Giờ: "+listTransaction.get(position).getTime());
                if(listTransaction.get(position).getAction().equals("buy")) {
                    tvActionTransaction.setText("Phương thức: Mua hàng");
                    tvNameInTransactionInPopUp.setText("Người bán: "+listTransaction.get(position).getPerformer());
                    tvChangeValueTransactionInPopUp.setText("Tổng vốn: - "+NumberTextWatcherForThousand.getDecimalFormattedString(listTransaction.get(position).getChangeValue()));
                }else if (listTransaction.get(position).getAction().equals("sell")){
                    tvActionTransaction.setText("Phương thức: Bán hàng");
                    tvNameInTransactionInPopUp.setText("Người mua: "+listTransaction.get(position).getPerformer());
                    tvChangeValueTransactionInPopUp.setText("Tổng vốn: + "+NumberTextWatcherForThousand.getDecimalFormattedString(listTransaction.get(position).getChangeValue()));
                }
                else{
                    tvActionTransaction.setText("Phương thức: Tự thêm vốn");
                    tvNameInTransactionInPopUp.setText("Người thêm vốn: "+listTransaction.get(position).getPerformer());
                    tvChangeValueTransactionInPopUp.setText("Tổng vốn: + "+NumberTextWatcherForThousand.getDecimalFormattedString(listTransaction.get(position).getChangeValue()));
                }

                btnCancelPopupViewDetailTransaction.setOnClickListener(v2->{dialogViewDetailTransaction.dismiss();});

                dialogViewDetailTransaction.show();
            });

            return v;
        }
    }
}