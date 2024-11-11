package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public class LoginActivity extends AppCompatActivity {

    private EditText _txtEmail, _txtPassword;
    private Button _btnLogin, _btnRegister;
    private TextView _txtResetPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        _txtEmail = findViewById(R.id.txt_LoginEmailAddress);
        _txtPassword = findViewById(R.id.txt_LoginPassword);
        _btnLogin = findViewById(R.id.btn_Login);
        _btnRegister = findViewById(R.id.btn_Register);
        _txtResetPassword = findViewById(R.id.txt_LoginPassword);

        _btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(_txtEmail.getText().toString().isEmpty() || _txtPassword.getText().toString().isEmpty())
                {
                    Toast.makeText(LoginActivity.this,"Hãy nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String email = _txtEmail.getText().toString().trim();
                    String password = _txtPassword.getText().toString().trim();

                    Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            Toast.makeText(LoginActivity.this,"Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            LoginActivity.this.finish();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(LoginActivity.this,"Đăng nhập thất bại, lỗi: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }, true);

                }
            }
        });

        _btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("LoginActivity", "here");
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        _txtResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}