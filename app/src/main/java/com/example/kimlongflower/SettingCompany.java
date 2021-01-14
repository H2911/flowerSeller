package com.example.kimlongflower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SettingCompany extends AppCompatActivity {

    private EditText edCompanyName;
    private Spinner sTypeOfCompany;
    private EditText edAddress;
    private EditText edCity;
    private EditText edProvince;
    private EditText edPhoneNumber1;
    private EditText edPhoneNumber2;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_company);

        TextView tvCancelSettingCompanyInfo = findViewById(R.id.tvCancelSettingCompanyInfo);
        tvCancelSettingCompanyInfo.setOnClickListener(v -> {
            finish();
        });

        edCompanyName = findViewById(R.id.edCompanyName);
        sTypeOfCompany = findViewById(R.id.sTypeOFCompany);
        edAddress = findViewById(R.id.edAddress);
        edCity = findViewById(R.id.edCity);
        edProvince = findViewById(R.id.edProvince);
        edPhoneNumber1 = findViewById(R.id.edPhoneNumber1);
        edPhoneNumber2 = findViewById(R.id.edPhoneNumber2);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.typeOfCompany, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        sTypeOfCompany.setAdapter(adapter);
        sTypeOfCompany.setSelection(0);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference  = firebaseDatabase.getReference("Users").child(userId);

        //get company info if registered
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("companyInfo")){
                    databaseReference.child("companyInfo").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Company company = snapshot.getValue(Company.class);
                            sTypeOfCompany.setSelection(adapter.getPosition(company.getTypeOfCompany()));
                            edCompanyName.setText(company.getCompanyName());
                            edAddress.setText(company.getAddress());
                            edCity.setText(company.getCity());
                            edProvince.setText(company.getProvince());
                            edPhoneNumber1.setText(company.getPhone1());
                            edPhoneNumber2.setText(company.getPhone2());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        Button btnConfirmCompanyInfo = findViewById(R.id.btnConfirmCompanyInfo);
        btnConfirmCompanyInfo.setOnClickListener(v->{
            if(edCompanyName.getText().toString().trim().matches("")){
                Toast.makeText(SettingCompany.this, "Tên công ty không thể bỏ trống", Toast.LENGTH_SHORT).show();
            }
            else if(edAddress.getText().toString().trim().matches("")){
                Toast.makeText(SettingCompany.this, "Địa chỉ công ty không thể bỏ trống", Toast.LENGTH_SHORT).show();
            }
            else if(edCity.getText().toString().trim().matches("")){
                Toast.makeText(SettingCompany.this, "Thông tin thành phố không thể bỏ trống", Toast.LENGTH_SHORT).show();
            }
            else if(edCity.getText().toString().trim().matches("")){
                Toast.makeText(SettingCompany.this, "Thông tin tỉnh không thể bỏ trống", Toast.LENGTH_SHORT).show();
            }
            else {
                final AlertDialog.Builder alertConfirmCompanyInfo = new AlertDialog.Builder(SettingCompany.this);
                alertConfirmCompanyInfo.setTitle("Xác nhận thông tin!");
                Company company = getCompanyInfo();
                alertConfirmCompanyInfo.setMessage("Tên công ty: " + company.getCompanyName() + "\n"
                        + "Mô hình doanh nghiệp: " + company.getTypeOfCompany()+"\n"
                        + "Địa chỉ:" + company.getAddress() + "\n"
                        + "Thành phố: " + company.getCity() +", tỉnh: "+ company.getProvince() +"\n"
                        + "Số điện thoại 1: " + company.getPhone1() + "\n"
                        + "Số điện thoại 2: " + company.getPhone2());
                alertConfirmCompanyInfo.setPositiveButton("Xác nhận", ((dialog, which) -> {
                    saveCompanyInfo(company);
                }));
                alertConfirmCompanyInfo.setNegativeButton("Hủy", ((dialog, which) -> {
                }));
                alertConfirmCompanyInfo.create().show();
            }
        });
    }

    private void saveCompanyInfo(Company company) {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        String userId = firebaseUser.getUid();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference  = firebaseDatabase.getReference("Users").child(userId).child("companyInfo");
        databaseReference.setValue(company).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                Toast.makeText(SettingCompany.this, "Lưu thông tin thành công!", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(SettingCompany.this, "Lưu thông tin thất bại!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Company getCompanyInfo(){
        edCompanyName = findViewById(R.id.edCompanyName);
        sTypeOfCompany = findViewById(R.id.sTypeOFCompany);
        edAddress = findViewById(R.id.edAddress);
        edCity = findViewById(R.id.edCity);
        edProvince = findViewById(R.id.edProvince);
        edPhoneNumber1 = findViewById(R.id.edPhoneNumber1);
        edPhoneNumber2 = findViewById(R.id.edPhoneNumber2);

        return new Company(sTypeOfCompany.getSelectedItem().toString(), edCompanyName.getText().toString(), edAddress.getText().toString(),edProvince.getText().toString(),edCity.getText().toString(),edPhoneNumber1.getText().toString(), edPhoneNumber2.getText().toString());
    }
}