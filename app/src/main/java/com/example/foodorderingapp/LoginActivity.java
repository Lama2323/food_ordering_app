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

public class LoginActivity extends AppCompatActivity {

    private EditText _txtEmail, _txtPassword;
    private Button _btnLogin, _btnRegister;
    private TextView _txtResetPassword;
    private boolean isRedirectFromOtherActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if redirected from other activity
        isRedirectFromOtherActivity = getIntent().getBooleanExtra("fromActivity", false);

        // Init views
        initViews();
        setupClickListeners();

        // Only check auto login if not redirected
        if (!isRedirectFromOtherActivity) {
            checkAutoLogin();
        }
    }

    private void initViews() {
        _txtEmail = findViewById(R.id.txt_LoginEmailAddress);
        _txtPassword = findViewById(R.id.txt_LoginPassword);
        _btnLogin = findViewById(R.id.btn_Login);
        _btnRegister = findViewById(R.id.btn_Register);
        _txtResetPassword = findViewById(R.id.txt_LoginPassword);
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
            _btnLogin.setText("Đang đăng nhập...");

            loginUser(email, password);
        });

        // Register button click
        _btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Reset password click
        _txtResetPassword.setOnClickListener(v -> {
            // Handle reset password
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
                _btnLogin.setText("Đăng nhập");

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

    private void checkAutoLogin() {
        // Show loading nếu cần
        Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
            @Override
            public void handleResponse(Boolean response) {
                if(response) {
                    String userObjectId = UserIdStorageFactory.instance().getStorage().get();

                    Backendless.Data.of(BackendlessUser.class).findById(userObjectId, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            // Auto login successful - chuyển thẳng đến ProductActivity
                            Toast.makeText(LoginActivity.this,
                                    "Đang đăng nhập với tài khoản đã lưu...",
                                    Toast.LENGTH_SHORT).show();

                            startActivity(new Intent(LoginActivity.this, ProductActivity.class));
                            LoginActivity.this.finish();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            // Auto login failed - ở lại màn login
                            Log.e("LoginActivity", "Auto login error: " + fault.getMessage());
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e("LoginActivity", "Check valid login error: " + fault.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        // Sửa lại logic onBackPressed
        if(isRedirectFromOtherActivity) {
            finishAffinity(); // Đóng tất cả activity và thoát app
        } else {
            super.onBackPressed();
        }
    }
}