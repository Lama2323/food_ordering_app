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
import com.example.foodorderingapp.utils.NetworkUtils;

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

        //khởi tạo tránh crash app
        Backendless.initApp(
                this,
                Environment.APPLICATION_ID,
                Environment.API_KEY
        );

        if (!NetworkUtils.isNetworkConnected(this)) {
            Intent intent = new Intent(this, DisconnectedActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_main);
        // Code khác của MainActivity

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

        //keep user login
        Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
            @Override
            public void handleResponse(Boolean response) {
                if (response)
                {
                    String userObjectId = UserIdStorageFactory.instance().getStorage().get();
                    Backendless.Data.of(BackendlessUser.class).findById(userObjectId, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                            LoginActivity.this.finish();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(LoginActivity.this,"Lỗi: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(LoginActivity.this,"Lỗi: " + fault.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}