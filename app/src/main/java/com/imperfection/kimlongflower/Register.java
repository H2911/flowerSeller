package com.imperfection.kimlongflower;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    DatabaseReference databaseReference;
    FirebaseAuth firebaseAuth;
    EditText edEmail;
    EditText edPassword;
    EditText edPassword2;
    EditText edPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        //When click submit register new user to database
        Button registerButton = findViewById(R.id.register2);
        registerButton.setOnClickListener(v -> {
            edEmail = findViewById(R.id.registeredUserName);
            edPassword = findViewById(R.id.registeredPassword);
            edPhoneNumber = findViewById(R.id.registeredPhoneNumber);
            edPassword2 = findViewById(R.id.repeatRegisteredPassword);
            String email = edEmail.getText().toString();
            String password = edPassword.getText().toString();
            String password2 = edPassword2.getText().toString();
            String phoneNumber = edPhoneNumber.getText().toString();

            if (TextUtils.isEmpty(email)||TextUtils.isEmpty(password)||TextUtils.isEmpty(password2))
            {
                Toast.makeText(Register.this,"Thiếu thông tin!!",Toast.LENGTH_SHORT).show();
            }
            else {
                if(password.matches(password2)){
                    RegisterNewUser(email,password,phoneNumber);
                }
                else {
                    Toast.makeText(Register.this,"Nhập lại mật khẩu không đúng!",Toast.LENGTH_SHORT).show();
                    edPassword.setText("");
                    edPassword2.setText("");
                }
            }
        });

        //Return Login page by arrow
        TextView tvReturnLogin = findViewById(R.id.tvReturnLogin);
        tvReturnLogin.setOnClickListener(v -> {
            finish();//End registration page
        });
    }

    //Return Login Page by back button
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();//End registration page
        }
        return super.onKeyDown(keyCode, event);
    }

    public void RegisterNewUser(String email, String password, String phoneNumber){
        firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                String userId = firebaseUser.getUid();
                databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                HashMap<String,String> userInfo = new HashMap<>();
                userInfo.put("email",email);
                userInfo.put("userId",userId);
                userInfo.put("phone number",phoneNumber);
                HashMap<String,String> emptyStorage = CreateEmptyDatabase.CreateEmptyDatabase();
                databaseReference.setValue(userInfo).addOnCompleteListener(task1 -> {
                    if(task1.isSuccessful()){
                        databaseReference.child("storage").setValue(emptyStorage).addOnCompleteListener(task11 -> {
                            if(task11.isSuccessful()){
                                Toast.makeText(Register.this,"Đăng ký thành công!",Toast.LENGTH_LONG).show();
                                finish();
                            }else {
                                Toast.makeText(Register.this, task11.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else {
                        Toast.makeText(Register.this, task1.getException().getMessage(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                Toast.makeText(Register.this, task.getException().getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }
}