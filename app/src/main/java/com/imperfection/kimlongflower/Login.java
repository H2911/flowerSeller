package com.imperfection.kimlongflower;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private EditText edUserName;
    private EditText edPassword;
    private TextView tvResetPassword;
    CheckBox cbRememberUser;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    String USERNAME_KEY = "user";
    String PASSWORD_KEY = "password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        firebaseAuth = FirebaseAuth.getInstance();

        edUserName = findViewById(R.id.userName);
        edPassword = findViewById(R.id.password);
        tvResetPassword = findViewById(R.id.tvForgotPassword);
        sharedPreferences = getSharedPreferences("loginPrefs",MODE_PRIVATE);
        edUserName.setText(sharedPreferences.getString(USERNAME_KEY,""));
        edPassword.setText(sharedPreferences.getString(PASSWORD_KEY,""));

        //Fetch data from firebase, login to application
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            edUserName = findViewById(R.id.userName);
            edPassword = findViewById(R.id.password);
            String userName = edUserName.getText().toString();
            String password = edPassword.getText().toString();
            if(TextUtils.isEmpty(userName)){
                Toast.makeText(Login.this,"Tài khoản còn thiếu!",Toast.LENGTH_SHORT).show();
            }else if(TextUtils.isEmpty(password)){
                Toast.makeText(Login.this,"Mật khẩu còn thiếu!",Toast.LENGTH_SHORT).show();
            }
            else{
                Login(userName,password);
            }
        });

        //Direct to registration page when click register button
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this,Register.class);//Initial registration page
            startActivity(intent);
            resetUserNameText();
            resetPasswordText();
        });

        tvResetPassword.setOnClickListener(v -> {
            EditText resetMail = new EditText(v.getContext());
            AlertDialog.Builder alertResetPassword = new AlertDialog.Builder(Login.this);
            alertResetPassword.setTitle("Đặt lại mật khẩu!");
            alertResetPassword.setMessage("Nhập mail cần đặt lại mật khẩu: ");
            alertResetPassword.setView(resetMail);
            alertResetPassword.setPositiveButton("Xác nhận",((dialog, which) -> {
                firebaseAuth.sendPasswordResetEmail(resetMail.getText().toString()).addOnSuccessListener(aVoid -> {
                    Toast.makeText(Login.this,"Link đặt lại mật khẩu đã được gửi tới mail!",Toast.LENGTH_LONG).show();
                }).addOnFailureListener(e -> {
                    Toast.makeText(Login.this,"Gửi xác nhận thất bại, thử lại sau ít phút!",Toast.LENGTH_SHORT).show();
                });
            }));
            alertResetPassword.setNegativeButton("Hủy",((dialog, which) -> { }));
            alertResetPassword.create().show();
        });
    }

    //Quit application
    @Override
    public void onBackPressed(){
        final AlertDialog.Builder quitAlert = new AlertDialog.Builder(Login.this);
        quitAlert.setMessage("Thoát ứng dụng?");
        quitAlert.setPositiveButton("Có", (dialog, which) -> android.os.Process.killProcess(android.os.Process.myPid()));
        quitAlert.setNegativeButton("Không", (dialog, which) -> {
        });
        quitAlert.create().show();
    }

    protected void resetUserNameText(){
        EditText username = findViewById(R.id.userName);
        username.setText("");
    }
    protected void resetPasswordText(){
        EditText password = findViewById(R.id.password);
        password.setText("");
    }

    private void Login(String userName, String password){
        firebaseAuth.signInWithEmailAndPassword(userName.trim(),password.trim()).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                cbRememberUser = findViewById(R.id.cbRememberUser);
                edUserName = findViewById(R.id.userName);
                edPassword = findViewById(R.id.password);
                if(cbRememberUser.isChecked()){
                    editor = sharedPreferences.edit();
                    editor.putString(USERNAME_KEY,edUserName.getText().toString().trim());
                    editor.putString(PASSWORD_KEY,edPassword.getText().toString());
                    editor.apply();
                }
                Intent intent = new Intent(Login.this, Main.class);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                startActivity(intent);
            }else {
                Toast.makeText(Login.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}

