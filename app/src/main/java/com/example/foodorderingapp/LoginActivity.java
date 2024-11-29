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
import com.backendless.persistence.local.UserIdStorageFactory;

public class LoginActivity extends BaseNetworkActivity {

    private EditText _txtEmail, _txtPassword;
    private Button _btnLogin, _btnRegister;
    private TextView _txtForgetPassword;
    private boolean isRedirectFromOtherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        isRedirectFromOtherActivity = getIntent().getBooleanExtra("fromActivity", false);
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        _txtEmail = findViewById(R.id.txt_LoginEmailAddress);
        _txtPassword = findViewById(R.id.txt_LoginPassword);
        _btnLogin = findViewById(R.id.btn_Login);
        _btnRegister = findViewById(R.id.btn_Register);
        _txtForgetPassword = findViewById(R.id.txt_ForgetPassword);
    }

    private void setupClickListeners() {
        // Login button click
        _btnLogin.setOnClickListener(v -> {
            String email = _txtEmail.getText().toString().trim();
            String password = _txtPassword.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Hãy nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading
            _btnLogin.setEnabled(false);
            _btnRegister.setEnabled(false);

            loginUser(email, password);
        });

        // Register button click
        _btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Reset password click
        _txtForgetPassword.setOnClickListener(v -> {
            String email = _txtEmail.getText().toString().trim();

            if(email.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Vui lòng nhập email để lấy lại mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            _txtForgetPassword.setEnabled(false);

            Backendless.UserService.restorePassword(email, new AsyncCallback<Void>() {
                @Override
                public void handleResponse(Void response) {
                    Toast.makeText(LoginActivity.this,
                            "Đã gửi email khôi phục mật khẩu. Vui lòng kiểm tra hộp thư.",
                            Toast.LENGTH_LONG).show();
                    _txtForgetPassword.setEnabled(true);
                }

                @Override
                public void handleFault(BackendlessFault fault) {
                    String errorMessage = "Không thể gửi email khôi phục: ";

                    switch(fault.getCode()) {
                        case "3020":
                            errorMessage += "Email không tồn tại";
                            break;
                        case "3000":
                            errorMessage += "Email không đúng định dạng";
                            break;
                        default:
                            errorMessage += fault.getMessage();
                    }

                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    _txtForgetPassword.setEnabled(true);
                }
            });
        });
    }

    private void loginUser(String email, String password) {
        Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
            @Override
            public void handleResponse(BackendlessUser response) {
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                LoginActivity.this.finish();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // Reset button state
                _btnLogin.setEnabled(true);
                _btnRegister.setEnabled(true);


                // Show error
                String errorMessage = "Đăng nhập thất bại: ";

                // Map error codes to friendly messages
                switch(fault.getCode()) {
                    case "3003":
                        errorMessage += "Email hoặc mật khẩu không đúng";
                        break;
                    case "3000":
                        errorMessage += "Email không đúng định dạng";
                        break;
                    default:
                        errorMessage += fault.getMessage();
                }

                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }, true);
    }

    @Override
    public void onBackPressed() {
        if(isRedirectFromOtherActivity) {
            finishAffinity();
        } else {
            super.onBackPressed();
        }
    }
}