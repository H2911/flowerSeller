package com.imperfection.kimlongflower;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.printer.command.EscCommand;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.imperfection.kimlongflower.CreateInvoice.BLUETOOTH_REQUEST_CODE;
import static com.imperfection.kimlongflower.CreateInvoice.addItemDetails;
import static com.imperfection.kimlongflower.CreateInvoice.addLineRow;
import static com.imperfection.kimlongflower.CreateInvoice.addNameItem;
import static com.imperfection.kimlongflower.CreateInvoice.convertViewToBitmap;
import static com.imperfection.kimlongflower.DeviceConnFactoryManager.CONN_STATE_FAILED;

public class ViewHistoryOfInvoices extends AppCompatActivity {

    private TextView tvStartDate;
    private TextView tvEndDate;
    private DatePickerDialog.OnDateSetListener startDateSetListener;
    private DatePickerDialog.OnDateSetListener endDateSetListener;
    private String action;

    private static final int CONN_PRINTER = 0x12;
    public static final int MESSAGE_UPDATE_PARAMETER = 0x009;
    private static final int PRINTER_COMMAND_ERROR = 0x008;
    private byte[] esc = { 0x10, 0x04, 0x02 };

    public static Activity activityViewHistoryInvoice;

    FirebaseAuth firebaseAuth;
    FirebaseUser currentUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    List<Invoice> invoiceList = new ArrayList<>();
    GridView gvInvoices;
    CustomAdapter customAdapter;//custom grid view product
    CustomAdapterPopUp customAdapterPopUp;//custom gird view popup

    Dialog dialogInvoice;

    private ThreadPool threadPool;
    private int	id = 0;
    private static Invoice currentInvoice;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_history_of_invoices);

        activityViewHistoryInvoice=this;

        dialogInvoice = new Dialog(this);

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
        Button btnPickStartDate = findViewById(R.id.bntPickStartDate);
        btnPickStartDate.setOnClickListener(v -> {
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
                    String name;
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
                                        itemList.add(new Item(item.getKey().split(",")[0].trim(),quantity,item.getKey().split(",")[1].trim(),unit));
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

            // View more details of a invoice
            bntViewInvoice.setOnClickListener(v -> {
                TextView tvDateInvoiceInPopUp;
                TextView tvTimeInvoiceInPopUp;
                TextView tvNameInInvoiceInPopUp;
                GridView gvInvoicesInPopUp;
                TextView tvCancelPopUp;
                TextView tvSumOfInvoiceInPopUp;
                Button btnPrintAgain;

                dialogInvoice.setContentView(R.layout.activity_popup_invoice);
                tvDateInvoiceInPopUp = dialogInvoice.findViewById(R.id.tvDateInvoiceInPopUp);
                tvTimeInvoiceInPopUp = dialogInvoice.findViewById(R.id.tvTimeInvoiceInPopUp);
                tvNameInInvoiceInPopUp = dialogInvoice.findViewById(R.id.tvNameInInvoiceInPopUp);
                gvInvoicesInPopUp = dialogInvoice.findViewById(R.id.gvInvoicesInPopUp);
                tvSumOfInvoiceInPopUp = dialogInvoice.findViewById(R.id.tvSumOfInvoiceInPopUp);
                btnPrintAgain = dialogInvoice.findViewById(R.id.btnPrintAgain);

                tvDateInvoiceInPopUp.setText(invoiceList.get(position).getDate());
                tvTimeInvoiceInPopUp.setText(invoiceList.get(position).getTime());
                tvNameInInvoiceInPopUp.setText(invoiceList.get(position).getName());
                tvSumOfInvoiceInPopUp.setText("Tổng: "+ NumberTextWatcherForThousand.getDecimalFormattedString(String.valueOf(invoiceList.get(position).getSum()))+ " đ");
                btnPrintAgain.setOnClickListener(v1 -> {
                    currentInvoice = invoiceList.get(position);
                    printInvoice(v1);
                });

                //custom popup dialog
                customAdapterPopUp = new CustomAdapterPopUp(invoiceList.get(position), dialogInvoice.getContext());
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

    private void printInvoice(View view) {
        //Check CompanyInfo
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference referenceOfCompanyInfo = firebaseDatabase.getReference("Users").child(userId);

        referenceOfCompanyInfo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("companyInfo")){
                    //Connect BT
                    startActivityForResult( new Intent( ViewHistoryOfInvoices.this, BluetoothDeviceList.class ), BLUETOOTH_REQUEST_CODE );
                }else {
                    final AlertDialog.Builder alertFillInCompanyInfo = new AlertDialog.Builder(ViewHistoryOfInvoices.this);
                    alertFillInCompanyInfo.setMessage("Nhập thông tin công ty để thực hiện chức năng in Bluetooth!");
                    alertFillInCompanyInfo.setPositiveButton("Ok",((dialog, which) -> {
                        startActivity(new Intent(ViewHistoryOfInvoices.this,SettingCompany.class));
                    }));
                    alertFillInCompanyInfo.setNeutralButton("Hủy",((dialog, which) -> {
                    }));
                    alertFillInCompanyInfo.create().show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

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
            tvQuantityInPopUpInvoice.setText(NumberTextWatcherForThousand.getDecimalFormattedString(itemList.get(position).getQuantity())+" "+itemList.get(position).getUnit());
            tvPriceInPopUpInvoice.setText(NumberTextWatcherForThousand.getDecimalFormattedString(itemList.get(position).getPrice()));
            tvSumOfItemList.setText(NumberTextWatcherForThousand.getDecimalFormattedString(itemList.get(position).getSumOfItem())+" đ");

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

        if(yearOfDateStart<yearOfDateEnd){
            return true;
        }

        if(yearOfDateStart==yearOfDateEnd){
            if(monthOfDateStart<=monthOfDateEnd){
                return dayOfDateStart <= dayOfDateEnd;
            }
        }
        return false;
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        IntentFilter filter = new IntentFilter( DeviceConnFactoryManager.ACTION_CONN_STATE );
        registerReceiver( receiver, filter );
    }

    private BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onReceive( Context context, Intent intent )
        {
            String action = intent.getAction();
            if (DeviceConnFactoryManager.ACTION_CONN_STATE.equals(action)) {
                int state = intent.getIntExtra(DeviceConnFactoryManager.STATE, -1);
                switch (state) {
                    case DeviceConnFactoryManager.CONN_STATE_CONNECTING:
                        Utils.toast(ViewHistoryOfInvoices.this,getString(R.string.str_connecting));
                        break;
                    case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                        Utils.toast(ViewHistoryOfInvoices.this,getString(R.string.connected));

                        Invoice invoice = currentInvoice;
                        View v = View.inflate(ViewHistoryOfInvoices.this, R.layout.pre_print_invoice, null);
                        TableLayout tl;

                        //Custom invoice info
                        TextView tvTypeOfInvoice;
                        TextView tvAction;
                        TextView tvName;
                        TextView tvSumOfPreInvoice;
                        TextView tvDateTimePreInvoice;

                        tl = v.findViewById(R.id.tlItems);
                        tvTypeOfInvoice = v.findViewById(R.id.tvTypeOfInvoice);
                        tvAction = v.findViewById(R.id.tvAction);
                        tvName = v.findViewById(R.id.tvName);
                        tvSumOfPreInvoice = v.findViewById(R.id.tvSumOfPreInvoice);
                        tvDateTimePreInvoice = v.findViewById(R.id.tvDateTimePreInvoice);

                        tvName.setText(invoice.getName());
                        action = currentInvoice.getAction();
                        if (action.equals("buy")) {
                            tvTypeOfInvoice.setText("HÓA ĐƠN MUA HÀNG");
                            tvAction.setText("Người bán:");
                        }

                        int i = 1;
                        for (Item item : invoice.getListItem()) {
                            //Add item to PreInvoice
                            tl.addView(addNameItem(ViewHistoryOfInvoices.this, String.valueOf(i), item.getName()));

                            //Add detail of the item to PreInvoice
                            tl.addView(addItemDetails(ViewHistoryOfInvoices.this, item.getQuantity(), item.getPrice(), item.getUnit(), item.getSumOfItem()));

                            tl.addView(addLineRow(ViewHistoryOfInvoices.this));
                            i++;
                        }

                        tvSumOfPreInvoice.setText("Tổng: " + NumberTextWatcherForThousand.getDecimalFormattedString(String.valueOf(invoice.getSum())));

                        tvDateTimePreInvoice.setText(invoice.getDate() + " " + invoice.getTime());

                        TextView tvTypeOfCompany;
                        TextView tvCompanyName;
                        TextView tvAddress;
                        TextView tvProvince;
                        TextView tvPhone1;
                        TextView tvPhone2;

                        tvTypeOfCompany = v.findViewById(R.id.tvTypeOfCompany);
                        tvCompanyName = v.findViewById(R.id.tvCompanyName);
                        tvAddress = v.findViewById(R.id.tvAddress);
                        tvProvince = v.findViewById(R.id.tvProvince);
                        tvPhone1 = v.findViewById(R.id.tvPhone1);
                        tvPhone2 = v.findViewById(R.id.tvPhone2);

                        //Custom company info
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        String userId = user.getUid();

                        firebaseDatabase = FirebaseDatabase.getInstance();
                        DatabaseReference referenceOfCompanyInfo  = firebaseDatabase.getReference("Users").child(userId).child("companyInfo");
                        referenceOfCompanyInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Company company = snapshot.getValue(Company.class);

                                tvTypeOfCompany.setText(company.getTypeOfCompany());
                                tvCompanyName.setText(company.getCompanyName());
                                tvAddress.setText(company.getAddress());
                                tvProvince.setText(company.getCity()+", "+company.getProvince());

                                if(!company.getPhone1().matches("")){
                                    tvPhone1.setText("SĐT: "+ company.getPhone1());
                                }else{
                                    tvPhone1.setText("");
                                }

                                if(!company.getPhone2().matches("")) {
                                    tvPhone2.setText("SĐT"+company.getPhone2());
                                }else{
                                    tvPhone2.setText("");
                                }

                                //Print XML
                                final Bitmap bitmap = convertViewToBitmap(v);
                                threadPool = ThreadPool.getInstance();
                                threadPool.addTask(() -> {
                                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] == null ||
                                            !DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getConnState()) {
                                        mHandler.obtainMessage(CONN_PRINTER).sendToTarget();
                                        return;
                                    }
                                    if (DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].getCurrentPrinterCommand() == PrinterCommand.ESC) {
                                        EscCommand esc = new EscCommand();
                                        esc.addInitializePrinter();
                                        esc.addRastBitImage(bitmap, 366, 0);
                                        esc.addPrintAndLineFeed();
                                        DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].sendDataImmediately(esc.getCommand());
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        break;

                    case CONN_STATE_FAILED:
                        Utils.toast(ViewHistoryOfInvoices.this, getString(R.string.str_conn_fail));
                        break;
                    default:
                        break;
                }
            }
        }
    };
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage( Message msg )
        {
            switch ( msg.what )
            {
                case PRINTER_COMMAND_ERROR:
                    Utils.toast( ViewHistoryOfInvoices.this, getString( R.string.str_choice_printer_command ) );
                    break;
                case CONN_PRINTER:
                    Utils.toast( ViewHistoryOfInvoices.this, getString( R.string.str_cann_printer ) );
                    break;
                case MESSAGE_UPDATE_PARAMETER:
                    String strIp = msg.getData().getString( "Ip" );
                    String strPort = msg.getData().getString( "Port" );
                    /* 初始化端口信息 */
                    new DeviceConnFactoryManager.Build()
                            .setConnMethod( DeviceConnFactoryManager.CONN_METHOD.WIFI )
                            .setIp( strIp )
                            .setId( id )
                            .setPort( Integer.parseInt( strPort ) )
                            .build();
                    threadPool = ThreadPool.getInstance();
                    threadPool.addTask( new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort();
                        }
                    } );
                    break;
                default:
                    new DeviceConnFactoryManager.Build()
                            .setConnMethod( DeviceConnFactoryManager.CONN_METHOD.WIFI )
                            .setIp( "192.168.2.227" )
                            .setId( id )
                            .setPort( 9100 )
                            .build();
                    threadPool.addTask(() -> DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort());
                    break;
            }
        }
    };

    @Override
    protected void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        super.onActivityResult( requestCode, resultCode, data );
        if ( resultCode == RESULT_OK )
        {
            closePort();
            String macAddress = data.getStringExtra(BluetoothDeviceList.EXTRA_DEVICE_ADDRESS);
            new DeviceConnFactoryManager.Build()
                    .setId(id)
                    .setConnMethod(DeviceConnFactoryManager.CONN_METHOD.BLUETOOTH)
                    .setMacAddress(macAddress)
                    .build();
            threadPool = ThreadPool.getInstance();
            threadPool.addTask(() -> DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].openPort());
        }
    }

    private void closePort()
    {
        if ( DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id] != null &&DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort != null )
        {
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].reader.cancel();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort.closePort();
            DeviceConnFactoryManager.getDeviceConnFactoryManagers()[id].mPort = null;
        }
    }
}