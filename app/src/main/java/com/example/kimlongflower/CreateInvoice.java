package com.example.kimlongflower;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CreateInvoice extends AppCompatActivity implements PrintingCallback {

    EditText etSeller;
    public static Activity activityBuy;
    GridView gvItem;
    CustomAdapter customAdapter;
    List<Item> itemList;
    String action;
    TextView tvName;
    Printing printing;
    FirebaseDatabase firebaseDatabase;

    FirebaseUser user;
    DatabaseReference reference;
    DatabaseReference referenceUpdateData;
    DatabaseReference referenceOfFunds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_invoice);

        Printooth.INSTANCE.init(CreateInvoice.this);
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
        ImageButton bntAddProduct = findViewById(R.id.iBntAddProduct);
        bntAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(CreateInvoice.this, ListProduct.class).putExtra("action",action);
            startActivity(intent);
        });

        //Create new invoice
        Button bntCreateNewInvoice = findViewById(R.id.bntCreateNewInvoice);
        bntCreateNewInvoice.setOnClickListener(v -> {
            if(printing!=null){
                printing.setPrintingCallback(this);
            }
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
                emptyNameAlert.setMessage("Tên " + tvName.getText().toString().split(":")[0].toLowerCase() + "không được bỏ trống");
                emptyNameAlert.setPositiveButton("OK", (dialog, which) -> {
                });
                emptyNameAlert.create().show();
            } else if (!Main.productsList.isEmpty()) {
                if (!Printooth.INSTANCE.hasPairedPrinter()) {
                    startActivityForResult(new Intent(CreateInvoice.this, ScanningActivity.class), ScanningActivity.SCANNING_FOR_PRINTER);
                } else{
                    printInvoice();
                }
            }
        });
    }

    private void printInvoice() {
        //Create invoice
        etSeller = findViewById(R.id.etSeller);
        Invoice invoice = new Invoice(etSeller.getText().toString(), action, Main.productsList);

        //Print
        invoice.printInvoice();

        user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        //Reference used to update data
        referenceUpdateData = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("storage");

        //Reference used to save invoice
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("invoices").child(action).child(invoice.getDate()).child(invoice.getTime()).child(invoice.getName());
        HashMap<String, String> invoiceInfo = new HashMap<>();
        HashMap<String,Object> updateData = new HashMap<>();
        List<Item> itemList = invoice.getListItem();
        for (Item item : itemList) {
            invoiceInfo.put(item.getName(), item.getQuantity() + " " + item.getUnit()+" "+item.getPrice());

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
                    updateData.put(item.getName(), changedValue);
                    referenceUpdateData.updateChildren(updateData).addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            reference.setValue(invoiceInfo).addOnCompleteListener(task1 -> {
                                if(task1.isSuccessful()){
                                    Toast.makeText(CreateInvoice.this,"Đã lưu hóa đơn",Toast.LENGTH_SHORT).show();
                                    Main.productsList.clear();
                                    finish();
                                }
                                else {
                                    Toast.makeText(CreateInvoice.this,"Lưu hóa đơn thất bại!",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        else{
                            Toast.makeText(CreateInvoice.this,"Lưu dữ liệu thất bại",Toast.LENGTH_SHORT).show();
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
        this.recreate();
    }

    @Override
    public void connectingWithPrinter() {
        Toast.makeText(CreateInvoice.this,"Đang kết nối với máy in",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectionFailed(String s) {
        Toast.makeText(CreateInvoice.this,"Kết nối thất bại",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String s) {
        Toast.makeText(CreateInvoice.this,"Lỗi: "+s,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessage(String s) {
        Toast.makeText(CreateInvoice.this,s,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void printingOrderSentSuccessfully() {
        Toast.makeText(CreateInvoice.this,"In hóa đơn hoàn tất",Toast.LENGTH_SHORT).show();
        refreshPage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK){
            initPrinting();
        }
    }

    private void initPrinting(){
        if(!Printooth.INSTANCE.hasPairedPrinter()){
            printing = Printooth.INSTANCE.printer();
        }
        if(printing != null){
            printing.setPrintingCallback(this);
        }
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
            tvQuantity.setText(itemList.get(position).getQuantity()+" "+itemList.get(position).getUnit());
            tvPrice.setText(itemList.get(position).getPrice());
            tvSumOfItem.setText(itemList.get(position).getSumOfItem());

            ibDelete.setOnClickListener(v -> {
                final AlertDialog.Builder backMainPageAlert = new AlertDialog.Builder(CreateInvoice.this);
                backMainPageAlert.setMessage("Xóa "+ tvNameItem.getText() +" ?");
                backMainPageAlert.setPositiveButton("Có", (dialog, which) -> {
                    Main.productsList.remove(itemList.get(position));
                    CreateInvoice.activityBuy.recreate();
                });
                backMainPageAlert.setNegativeButton("Không", (dialog, which) -> {
                });
                backMainPageAlert.create().show();
            });

            return view;
        }
    }
}