package com.example.kimlongflower;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ViewHistoryOfInvoices extends AppCompatActivity {

    private TextView tvStartDate;
    private TextView tvEndDate;
    private DatePickerDialog.OnDateSetListener startDateSetListener;
    private DatePickerDialog.OnDateSetListener endDateSetListener;
    private String action;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    List<Invoice> invoiceList = new ArrayList<>();
    GridView gvInvoices;
    CustomAdapter customAdapter;//custom grid view product
    CustomAdapterPopUp customAdapterPopUp;//custom gird view popup

    Dialog dialogInvoice;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history_of_invoices);

        Date currentDate = new Date();
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        tvStartDate.setText(formatterDate.format(currentDate));
        tvEndDate.setText(formatterDate.format(currentDate));

        action = (String) getIntent().getSerializableExtra("action");

        TextView tvTittle = findViewById(R.id.tvTittle);
        if(action.equals("buy")){
            tvTittle.setText("Lịch sử mua hàng");
        }

        //Pick start date
        Button bntPickStartDate = findViewById(R.id.bntPickStartDate);
        bntPickStartDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int date = calendar.get(Calendar.DATE);

            DatePickerDialog dialog = new DatePickerDialog(
                    ViewHistoryOfInvoices.this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    startDateSetListener,year,month,date);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        //set start date
        startDateSetListener = (view, year, month, dayOfMonth) -> {
            month +=1;
            String startDate = dayOfMonth+"/"+month+"/"+year;
            tvEndDate = findViewById(R.id.tvEndDate);
            String endDate = tvEndDate.getText().toString();
            tvStartDate.setText(startDate);
            if(!isSearchDateValid(startDate,endDate)) {
                tvEndDate.setText(startDate);
            }
        };

        //Pick end date
        Button bntPickEndDate = findViewById(R.id.bntPickEndDate);
        bntPickEndDate.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int date = calendar.get(Calendar.DATE);

            DatePickerDialog dialog = new DatePickerDialog(
                    ViewHistoryOfInvoices.this,
                    android.R.style.Theme_Holo_Light_Dialog,
                    endDateSetListener,year,month,date);
            dialog.getDatePicker().setMaxDate(new Date().getTime());
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.show();
        });

        //set end date
        endDateSetListener = (view, year, month, dayOfMonth) -> {
            month +=1;
            String endDate = dayOfMonth+"/"+month+"/"+year;
            tvStartDate = findViewById(R.id.tvStartDate);
            String startDate = tvStartDate.getText().toString();
            tvEndDate.setText(endDate);
            if(!isSearchDateValid(startDate,endDate)) {
                tvStartDate.setText(endDate);
            }
        };

        //Search History
        Button bntSearchHistory = findViewById(R.id.bntSearchHistory);
        bntSearchHistory.setOnClickListener(v -> {
            invoiceList = new ArrayList<>();

            firebaseAuth = FirebaseAuth.getInstance();
            currentUser = firebaseAuth.getCurrentUser();
            String userId = currentUser.getUid();

            tvStartDate = findViewById(R.id.tvStartDate);
            tvEndDate = findViewById(R.id.tvEndDate);

            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference("Users").child(userId).child("invoices").child(action);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String name = "";
                    //get date from database
                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                        //check query
                        if(isDateBetweenStartDateAndEndDate(tvStartDate.getText().toString(),tvEndDate.getText().toString(),dataSnapshot.getKey())){
                            //get time data from selected date
                            for(DataSnapshot timeData:dataSnapshot.getChildren()){
                                //get seller or buyer name
                                for(DataSnapshot nameData:timeData.getChildren()){
                                    name = nameData.getKey();
                                    List<Item> itemList = new ArrayList<>();
                                    //get item list in invoice
                                    for(DataSnapshot item: nameData.getChildren()){
                                        String quantity = item.getValue().toString().split(" ")[0];
                                        String unit = item.getValue().toString().split(" ")[1];
                                        String price = item.getValue().toString().split(" ")[2];
                                        itemList.add(new Item(item.getKey(),quantity,price,unit));
                                    }
                                    invoiceList.add(new Invoice(name,action,itemList,dataSnapshot.getKey(),timeData.getKey()));
                                }
                            }
                        }
                    }
                    customAdapter.notifyDataSetChanged();
                    if(invoiceList.isEmpty()){
                        Toast.makeText(ViewHistoryOfInvoices.this,"Không có lịch sử để hiển thị!",Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });

            //Show invoice in grid view
            gvInvoices = findViewById(R.id.gvInvoices);
            customAdapter = new CustomAdapter(invoiceList,this);
            gvInvoices.setAdapter(customAdapter);
        });


        Button bntBackSeparateHistoryPage = findViewById(R.id.bntBackSeparateHistoryPage);
        bntBackSeparateHistoryPage.setOnClickListener(v->{finish();});

        ImageButton ibBackSeparateHistoryPage = findViewById(R.id.ibBackSeparateHistoryPage);
        ibBackSeparateHistoryPage.setOnClickListener(v->{finish();});
    }

    public class CustomAdapter extends BaseAdapter{

        private final List<Invoice> invoiceList;
        private final Context context;

        public CustomAdapter(List<Invoice> invoiceList, Context context) {
            this.invoiceList = invoiceList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return invoiceList.size();
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
            View view = getLayoutInflater().inflate(R.layout.row_invoice,null);
            TextView tvNameInInvoice = view.findViewById(R.id.tvNameInInvoice);
            TextView tvDateInvoice = view.findViewById(R.id.tvDateInvoice);
            TextView tvTimeInvoice = view.findViewById(R.id.tvTimeInvoice);
            Button bntViewInvoice = view.findViewById(R.id.bntViewInvoice);

            tvNameInInvoice.setText(invoiceList.get(position).getName());
            tvDateInvoice.setText(invoiceList.get(position).getDate());
            tvTimeInvoice.setText(invoiceList.get(position).getTime());
            bntViewInvoice.setOnClickListener(v -> {
                TextView tvDateInvoiceInPopUp;
                TextView tvTimeInvoiceInPopUp;
                TextView tvNameInInvoiceInPopUp;
                GridView gvInvoicesInPopUp;
                TextView tvCancelPopUp;
                TextView tvSumOfInvoiceInPopUp;

                dialogInvoice.setContentView(R.layout.activity_popup_invoice);
                tvDateInvoiceInPopUp = dialogInvoice.findViewById(R.id.tvDateInvoiceInPopUp);
                tvTimeInvoiceInPopUp = dialogInvoice.findViewById(R.id.tvTimeInvoiceInPopUp);
                tvNameInInvoiceInPopUp = dialogInvoice.findViewById(R.id.tvNameInInvoiceInPopUp);
                gvInvoicesInPopUp = dialogInvoice.findViewById(R.id.gvInvoicesInPopUp);
                tvSumOfInvoiceInPopUp = dialogInvoice.findViewById(R.id.tvSumOfInvoiceInPopUp);

                tvDateInvoiceInPopUp.setText(invoiceList.get(position).getDate());
                tvTimeInvoiceInPopUp.setText(invoiceList.get(position).getTime());
                tvNameInInvoiceInPopUp.setText(invoiceList.get(position).getName());
                tvSumOfInvoiceInPopUp.setText(String.valueOf(invoiceList.get(position).getSum()));

                //custom popup dialog
                customAdapterPopUp = new CustomAdapterPopUp(invoiceList.get(position),dialogInvoice.getContext());
                gvInvoicesInPopUp.setAdapter(customAdapterPopUp);

                tvCancelPopUp = dialogInvoice.findViewById(R.id.tvCancelPopUp);
                tvCancelPopUp.setOnClickListener(v1->{
                    dialogInvoice.dismiss();
                });
                dialogInvoice.show();
            });

            return view;
        }
    }

    //Input String: dd/mm/yy (startDate, endDate), dd_mm_yy (dateNeedCheck)
    //Check date is between start date and end date
    public static boolean isDateBetweenStartDateAndEndDate(String startDate, String endDate, String dateNeedCheck){
        int yearOfDateStart = Integer.parseInt(startDate.split("/")[2]);
        int monthOfDateStart = Integer.parseInt(startDate.split("/")[1]);
        int dayOfDateStart = Integer.parseInt(startDate.split("/")[0]);

        int yearOfDateEnd = Integer.parseInt(endDate.split("/")[2]);
        int monthOfDateEnd = Integer.parseInt(endDate.split("/")[1]);
        int dayOfDateEnd = Integer.parseInt(endDate.split("/")[0]);

        int yearOfDateNeedCheck = Integer.parseInt(dateNeedCheck.split("_")[2]);
        int monthOfDateNeedCheck = Integer.parseInt(dateNeedCheck.split("_")[1]);
        int dayOfDateNeedCheck = Integer.parseInt(dateNeedCheck.split("_")[0]);

        if(yearOfDateNeedCheck == yearOfDateEnd && yearOfDateNeedCheck == yearOfDateStart){
            if (monthOfDateNeedCheck == monthOfDateEnd && monthOfDateNeedCheck == monthOfDateStart){
                return dayOfDateNeedCheck <= dayOfDateEnd && dayOfDateNeedCheck >= dayOfDateStart;
            }else return monthOfDateNeedCheck <= monthOfDateEnd && monthOfDateNeedCheck >= monthOfDateStart;
        }else return yearOfDateNeedCheck <= yearOfDateEnd && yearOfDateNeedCheck >= yearOfDateStart;
    }

    public class CustomAdapterPopUp extends BaseAdapter{
        private Invoice invoice;
        private Context context;
        private List<Item> itemList;

        public CustomAdapterPopUp(Invoice invoice, Context context) {
            this.invoice = invoice;
            this.context = context;
            itemList = invoice.getListItem();
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int position) {
            return 0;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = getLayoutInflater().inflate(R.layout.row_popup_invoice,null);

            TextView tvItemNameInPopUpInvoice = view.findViewById(R.id.tvItemNameInPopUpInvoice);
            TextView tvQuantityInPopUpInvoice = view.findViewById(R.id.tvQuantityInPopUpInvoice);
            TextView tvPriceInPopUpInvoice = view.findViewById(R.id.tvPriceInPopUpInvoice);
            TextView tvSumOfItemList = view.findViewById(R.id.tvSumOfItemList);

            tvItemNameInPopUpInvoice.setText(itemList.get(position).getName());
            tvQuantityInPopUpInvoice.setText(itemList.get(position).getQuantity()+" "+itemList.get(position).getUnit());
            tvPriceInPopUpInvoice.setText(itemList.get(position).getPrice());
            tvSumOfItemList.setText(itemList.get(position).getSumOfItem());

            return view;
        }
    }

    public static boolean isSearchDateValid(String startDate, String endDate){
        int yearOfDateStart = Integer.parseInt(startDate.split("/")[2]);
        int monthOfDateStart = Integer.parseInt(startDate.split("/")[1]);
        int dayOfDateStart = Integer.parseInt(startDate.split("/")[0]);

        int yearOfDateEnd = Integer.parseInt(endDate.split("/")[2]);
        int monthOfDateEnd = Integer.parseInt(endDate.split("/")[1]);
        int dayOfDateEnd = Integer.parseInt(endDate.split("/")[0]);

        if(yearOfDateStart<=yearOfDateEnd){
            if(monthOfDateStart<=monthOfDateEnd){
                return dayOfDateStart <= dayOfDateEnd;
            }
        }
        return false;
    }
}