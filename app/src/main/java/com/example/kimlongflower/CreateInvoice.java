package com.example.kimlongflower;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.printer.command.EscCommand;

import java.lang.ref.Reference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.example.kimlongflower.DeviceConnFactoryManager.CONN_STATE_FAILED;


public class CreateInvoice extends AppCompatActivity {
    ArrayList<String> per	= new ArrayList<>();
    private static final int	REQUEST_CODE = 0x004;
    public static final int MESSAGE_UPDATE_PARAMETER = 0x009;
    private static final int PRINTER_COMMAND_ERROR = 0x008;
    private byte[] esc = { 0x10, 0x04, 0x02 };

    private static final int CONN_PRINTER = 0x12;
    private String[] permissions = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH
    };
    private ThreadPool threadPool;

    private boolean printed = false;

    private int	id = 0;

    public static final int BLUETOOTH_REQUEST_CODE = 0x001;

    EditText etSeller;
    public static Activity activityBuy;
    GridView gvItem;
    public static CustomAdapter customAdapter;
    List<Item> itemList;
    String action;
    TextView tvName;

    FirebaseDatabase firebaseDatabase;
    FirebaseUser user;
    DatabaseReference referenceOfCompanyInfo;
    DatabaseReference reference;
    DatabaseReference referenceUpdateData;
    DatabaseReference referenceOfFunds;

    private static Invoice currentInvoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice);

        checkPermission();
        requestPermission();

        action = (String) getIntent().getSerializableExtra("action");

        tvName = findViewById(R.id.textView7);
        if(action.equals("sell")){
            tvName.setText("Người mua:");
        }

        setDateTime();
        activityBuy = this;
        itemList = Main.productsList;
        gvItem = findViewById(R.id.gvItemSell);
        customAdapter = new CustomAdapter(itemList,this);
        gvItem.setAdapter(customAdapter);

        //Add new product to list
        ImageButton btnAddProduct = findViewById(R.id.iBntAddProduct);
        btnAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(CreateInvoice.this, ListProduct.class).putExtra("action",action);
            startActivity(intent);
        });

        //Create new invoice
        Button bntCreateNewInvoice = findViewById(R.id.bntCreateNewInvoice);
        bntCreateNewInvoice.setOnClickListener(v -> {
            etSeller = findViewById(R.id.etSeller);
            if (!(Main.productsList.isEmpty() && etSeller.getText().toString().equals(""))) {
                final AlertDialog.Builder createNewInvoiceAlert = new AlertDialog.Builder(CreateInvoice.this);
                createNewInvoiceAlert.setMessage("Tạo hóa đơn mới?");
                createNewInvoiceAlert.setPositiveButton("Có", (dialog, which) -> refreshPage());
                createNewInvoiceAlert.setNegativeButton("Không", (dialog, which) -> {
                });
                createNewInvoiceAlert.create().show();
            }
        });

        //Back button
        Button bntBack = findViewById(R.id.bntBack);
        bntBack.setOnClickListener(v -> backMainPage());

        //Print invoice
        Button bntPrint = findViewById(R.id.bntPrint);
        bntPrint.setOnClickListener(v -> {
            etSeller = findViewById(R.id.etSeller);
            if (etSeller.getText().toString().trim().isEmpty()) {
                final AlertDialog.Builder emptyNameAlert = new AlertDialog.Builder(CreateInvoice.this);
                emptyNameAlert.setMessage("Tên " + tvName.getText().toString().split(":")[0].toLowerCase() + " không được bỏ trống");
                emptyNameAlert.setPositiveButton("OK", (dialog, which) -> {
                });
                emptyNameAlert.create().show();
            } else if (!Main.productsList.isEmpty()) {
                final AlertDialog.Builder alertPrint = new AlertDialog.Builder(CreateInvoice.this);
                alertPrint.setMessage("Lưu hóa đơn!");
                alertPrint.setPositiveButton("In và lưu hóa đơn", (dialog, which) ->{
                        currentInvoice = getCurrentInvoice();
                        printInvoice(v);
                });
                alertPrint.setNegativeButton("Lưu hóa đơn",(dialog, which) -> {
                    currentInvoice = getCurrentInvoice();
                    saveInvoice();
                });
                alertPrint.setNeutralButton("Hủy", (dialog, which) -> {
                });
                alertPrint.create().show();
            }
            else {
                Toast.makeText(CreateInvoice.this,"Danh sách mặt hàng còn trống!",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        IntentFilter filter = new IntentFilter( DeviceConnFactoryManager.ACTION_CONN_STATE );
        registerReceiver( receiver, filter );
    }

    private void checkPermission()
    {
        for ( String permission : permissions )
        {
            if ( PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission( this, permission ) )
            {
                per.add( permission );
            }
        }
    }


    private void requestPermission()
    {
        if ( per.size() > 0 )
        {
            String[] p = new String[per.size()];
            ActivityCompat.requestPermissions( this, per.toArray( p ), REQUEST_CODE );
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private TableRow addLineRow(Context context) {
        TableRow tb = new TableRow(context);
        tb.setLayoutParams( new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT  ) );
        TextView tv1 = new TextView( context );
        tv1.setLayoutParams( new TableRow.LayoutParams( TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT ) );
        tv1.setText( "- - - - - - - -" );
        tv1.setTextColor( Color.BLACK );
        tv1.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tv1.setTextSize( 15);
        tb.addView( tv1 );
        return tb;
    }

    public static Bitmap convertViewToBitmap( View view )
    {
        view.measure( View.MeasureSpec.makeMeasureSpec( 0, View.MeasureSpec.UNSPECIFIED ), View.MeasureSpec.makeMeasureSpec( 0, View.MeasureSpec.UNSPECIFIED ) );
        view.layout( 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight() );
        view.buildDrawingCache();
        Bitmap bitmap = view.getDrawingCache();
        return(bitmap);
    }


    private TableRow addNameItem( Context context, String number, String name)
    {
        TableRow tb = new TableRow( context );
        tb.setLayoutParams( new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT,TableLayout.LayoutParams.WRAP_CONTENT  ) );
        TextView tv1 = new TextView( context );
        tv1.setLayoutParams( new TableRow.LayoutParams( TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT ) );
        tv1.setText( number + ". "+name );
        tv1.setTextColor( Color.BLACK );
        tv1.setTextSize( 8 );
        tb.addView( tv1 );
        return(tb);
    }

    private TableRow addItemDetails( Context context, String quantity, String price,String unit, String sumOfItem  )
    {
        TableRow tb = new TableRow( context );
        tb.setLayoutParams( new TableLayout.LayoutParams( TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT ) );
        TextView tv2 = new TextView( context );
        tv2.setLayoutParams( new TableRow.LayoutParams( TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT ) );
        tv2.setText( NumberTextWatcherForThousand.getDecimalFormattedString(quantity)+" "+ unit+" x "+ NumberTextWatcherForThousand.getDecimalFormattedString(price) +" = "+ NumberTextWatcherForThousand.getDecimalFormattedString(sumOfItem ));
        tv2.setTextColor( Color.BLACK );
        tv2.setTextSize( 8 );
        tb.addView( tv2 );
        return(tb);
    }

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

    private void printInvoice(View view) {
        //Check CompanyInfo
        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        referenceOfCompanyInfo = firebaseDatabase.getReference("Users").child(userId);

        referenceOfCompanyInfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("companyInfo")){
                    //Connect BT
                    startActivityForResult( new Intent( CreateInvoice.this, BluetoothDeviceList.class ), BLUETOOTH_REQUEST_CODE );
                }else {
                    final AlertDialog.Builder alertFillInCompanyInfo = new AlertDialog.Builder(CreateInvoice.this);
                    alertFillInCompanyInfo.setMessage("Nhập thông tin công ty để thực hiện chức năng in Bluetooth!");
                    alertFillInCompanyInfo.setPositiveButton("Ok",((dialog, which) -> {
                        startActivity(new Intent(CreateInvoice.this,SettingCompany.class));
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

    public void onBackPressed(){
        backMainPage();
    }

    private void setDateTime(){
        Date date = new Date();
        SimpleDateFormat formatterDate = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat formatterTime = new SimpleDateFormat("hh:mm:ss");
        TextView tvDate = findViewById(R.id.tvDate);//get date
        tvDate.setText(formatterDate.format(date));//set date
        TextView tvTime = findViewById(R.id.tvTime);//get time
        tvTime.setText(formatterTime.format(date));//set time
    }

    //Back Main Page
    private void backMainPage(){
        etSeller = findViewById(R.id.etSeller);
        if(Main.productsList.isEmpty() && etSeller.getText().toString().trim().isEmpty()) {
            Main.productsList.clear();
            finish();
        }else{
            final AlertDialog.Builder backMainPageAlert = new AlertDialog.Builder(CreateInvoice.this);
            backMainPageAlert.setMessage("Hủy đơn đang thực hiện?");
            backMainPageAlert.setPositiveButton("Có", (dialog, which) -> {
                Main.productsList.clear();
                finish();
            });
            backMainPageAlert.setNegativeButton("Không", (dialog, which) -> {
            });
            backMainPageAlert.create().show();
        }
    }

    //Refresh page
    private void refreshPage(){
        setDateTime();
        Main.productsList.clear();
        etSeller = findViewById(R.id.etSeller);
        etSeller.setText("");
        customAdapter.notifyDataSetChanged();
        currentInvoice = null;
    }

    public class CustomAdapter extends BaseAdapter{
        private final List<Item> itemList;
        private final Context context;

        public CustomAdapter(List<Item> itemList, Context context) {
            this.itemList = itemList;
            this.context = context;
        }

        @Override
        public int getCount() {
            return itemList.size();
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
            View view = getLayoutInflater().inflate(R.layout.row_products,null);

            TextView tvNameItem = view.findViewById(R.id.tvItemNameList);
            TextView tvQuantity = view.findViewById(R.id.tvQuantityList);
            TextView tvPrice = view.findViewById(R.id.tvPriceList);
            TextView tvSumOfItem = view.findViewById(R.id.tvSumOfItemList);
            ImageButton ibDelete = view.findViewById(R.id.ibRemove);

            tvNameItem.setText(itemList.get(position).getName());
            tvQuantity.setText(NumberTextWatcherForThousand.getDecimalFormattedString(itemList.get(position).getQuantity())+" "+itemList.get(position).getUnit());
            tvPrice.setText(NumberTextWatcherForThousand.getDecimalFormattedString(itemList.get(position).getPrice()));
            tvSumOfItem.setText(NumberTextWatcherForThousand.getDecimalFormattedString(itemList.get(position).getSumOfItem())+ " đ");

            ibDelete.setOnClickListener(v -> {
                final AlertDialog.Builder backMainPageAlert = new AlertDialog.Builder(CreateInvoice.this);
                backMainPageAlert.setMessage("Xóa "+ tvNameItem.getText() +" ?");
                backMainPageAlert.setPositiveButton("Có", (dialog, which) -> {
                    Main.productsList.remove(itemList.get(position));
                    customAdapter.notifyDataSetChanged();
                });
                backMainPageAlert.setNegativeButton("Không", (dialog, which) -> {
                });
                backMainPageAlert.create().show();
            });

            return view;
        }
    }

    private void saveInvoice() {
        //Get invoice
        Invoice invoice = currentInvoice;

        //Authenticate user
        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        //Reference used to update data
        referenceUpdateData = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("storage");

        //Reference used to update funds
        referenceOfFunds = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("funds");

        //Add history of funds
        DatabaseReference historyOfFunds =  referenceOfFunds.child(action).child(invoice.getDate()).child(invoice.getTime()).child(invoice.getName());
        historyOfFunds.setValue(invoice.getSum()).addOnCompleteListener(task -> {
            if (task.isSuccessful()){
                HashMap<String,Object> updateData = new HashMap<>();
                referenceOfFunds.child("value").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String oldValue = String.valueOf(snapshot.getValue(Long.class));
                        String newValue = String.valueOf(invoice.getSum());

                        //Update data from storage
                        String changedValue;
                        if (action.equals("buy")) {
                            changedValue = String.valueOf(Long.parseLong(oldValue) - Long.parseLong(newValue));
                        } else {
                            changedValue = String.valueOf(Long.parseLong(oldValue) + Long.parseLong(newValue));
                        }
                        updateData.put("value", Long.parseLong(changedValue));

                        //updateFunds
                        referenceOfFunds.updateChildren(updateData).addOnCompleteListener(task -> {
                            if(task.isSuccessful()){
                                Toast.makeText(CreateInvoice.this,"Lưu vốn thành công",Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(CreateInvoice.this,"Lưu vốn thất bại",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(CreateInvoice.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //Reference used to save invoice
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("invoices").child(action).child(invoice.getDate()).child(invoice.getTime()).child(invoice.getName());
        HashMap<String, String> invoiceInfo = new HashMap<>();
        HashMap<String,Object> updateDataInvoice = new HashMap<>();
        List<Item> itemList = invoice.getListItem();
        for (Item item : itemList) {
            invoiceInfo.put(item.getName()+", "+item.getPrice(), item.getQuantity() + " " + item.getUnit());

            //Get data from storage
            referenceUpdateData.child(item.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String oldValue = snapshot.getValue().toString().split(" ")[0].trim();
                    String newValue = item.getQuantity();
                    String unit = snapshot.getValue().toString().split(" ")[1].trim();

                    //Update data from storage
                    String changedValue;
                    if(action.equals("buy")) {
                        changedValue = (Integer.parseInt(oldValue) + Integer.parseInt(newValue)) +" "+ unit;
                    }else{
                        changedValue = (Integer.parseInt(oldValue) - Integer.parseInt(newValue)) +" "+ unit;
                    }
                    updateDataInvoice.put(item.getName(), changedValue);

                    //Save new invoice
                    referenceUpdateData.updateChildren(updateDataInvoice).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            reference.setValue(invoiceInfo).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    Toast.makeText(CreateInvoice.this,"Đã lưu hóa đơn!" ,Toast.LENGTH_SHORT).show();
                                    printed = true;
                                    refreshPage();
                                    finish();
                                }
                                else {
                                    Toast.makeText(CreateInvoice.this,"Lưu hóa đơn thất bại!",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            Toast.makeText(CreateInvoice.this,"Lưu dữ liệu thất bại!",Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(CreateInvoice.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private Invoice getCurrentInvoice(){
        etSeller = findViewById(R.id.etSeller);
        return new Invoice(etSeller.getText().toString(), action, Main.productsList);
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
                        Utils.toast(CreateInvoice.this,getString(R.string.str_connecting));
                        break;
                    case DeviceConnFactoryManager.CONN_STATE_CONNECTED:
                        if(printed){
                            break;
                        }
                        Utils.toast(CreateInvoice.this,getString(R.string.connected));

                        Invoice invoice = currentInvoice;
                        View v = View.inflate(CreateInvoice.this, R.layout.pre_print_invoice, null);
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
                        action = (String) getIntent().getSerializableExtra("action");
                        if (action.equals("buy")) {
                            tvTypeOfInvoice.setText("HÓA ĐƠN MUA HÀNG");
                            tvAction.setText("Người bán:");
                        }

                        int i = 1;
                        for (Item item : invoice.getListItem()) {
                            //Add item to PreInvoice
                            tl.addView(addNameItem(CreateInvoice.this, String.valueOf(i), item.getName()));

                            //Add detail of the item to PreInvoice
                            tl.addView(addItemDetails(CreateInvoice.this, item.getQuantity(), item.getPrice(), item.getUnit(), item.getSumOfItem()));

                            tl.addView(addLineRow(CreateInvoice.this));
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
                        user = FirebaseAuth.getInstance().getCurrentUser();
                        String userId = user.getUid();

                        firebaseDatabase = FirebaseDatabase.getInstance();
                        referenceOfCompanyInfo  = firebaseDatabase.getReference("Users").child(userId).child("companyInfo");
                        referenceOfCompanyInfo.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Company company = snapshot.getValue(Company.class);

                                tvTypeOfCompany.setText(company.getTypeOfCompany());
                                tvCompanyName.setText(company.getCompanyName());
                                tvAddress.setText(company.getAddress());
                                tvProvince.setText(company.getCity()+", "+company.getProvince());

                                if(!company.getPhone1().matches("")){
                                    tvPhone1.setText(company.getPhone1());
                                }else{
                                    tvPhone1.setText("");
                                }

                                if(!company.getPhone2().matches("")) {
                                    tvPhone2.setText(company.getPhone2());
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
                                        saveInvoice();
                                    }
                                });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        break;

                    case CONN_STATE_FAILED:
                        Utils.toast(CreateInvoice.this, getString(R.string.str_conn_fail));
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
                    Utils.toast( CreateInvoice.this, getString( R.string.str_choice_printer_command ) );
                    break;
                case CONN_PRINTER:
                    Utils.toast( CreateInvoice.this, getString( R.string.str_cann_printer ) );
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
    protected void onDestroy()
    {
        super.onDestroy();
        DeviceConnFactoryManager.closeAllPort();
        if ( threadPool != null )
        {
            threadPool.stopThreadPool();
            threadPool = null;
        }
    }
}