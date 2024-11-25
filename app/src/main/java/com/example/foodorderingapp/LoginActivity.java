package com.example.foodorderingapp;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserIdStorageFactory;
import com.example.foodorderingapp.utils.NetworkUtils;

import java.util.List;
import java.util.Map;

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

        //Log.i("LoginActivity", "b4 init app");

        //khởi tạo tránh crash app
        Backendless.initApp(
                this,
                Environment.APPLICATION_ID,
                Environment.API_KEY
        );

        //Log.i("LoginActivity", "after init app");

        if (!NetworkUtils.isNetworkConnected(this)) {
            Intent intent = new Intent(this, DisconnectedActivity.class);
            startActivity(intent);
            Log.e("LoginActivity", "No Internet");
            finish();
        }

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

                    DataQueryBuilder queryBuilder = DataQueryBuilder.create();
                    queryBuilder.setWhereClause("email = '" + email + "' AND password = '" + password + "'");

                    Backendless.Data.of("user").find(queryBuilder, new AsyncCallback<List<Map>>() {
                        @Override
                        public void handleResponse(List<Map> foundUsers) {
                            if (foundUsers != null && !foundUsers.isEmpty())
                            {
                                Map user = foundUsers.get(0);

                                Log.v("LoginActivity", "Login success: " + user.get("email"));
                                Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                                //sau khi đăng nhập thành công, lưu email vào Prefs (tương tự như PlayerPrefs bên Unity) - lưu local
                                //để thực hiện truy vấn dựa trên cái mail người dùng
                                //Nên tạo 1 file riêng chứa các constant là các key này:
                                //vd: const string PrefsUserInfo = "UserInfo"; để dễ quản lý
                                SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
                                prefs.edit().putString("email", email).apply();

                                Intent intent = new Intent(LoginActivity.this, CartActivity.class);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                Log.e("LoginActivity", "User " + email + "not found");
                                Toast.makeText(LoginActivity.this, "Sai email hoặc mật khẩu", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault fault)
                        {
                            Log.e("LoginActivity", "Login failed: " + fault.getMessage());
                            Toast.makeText(LoginActivity.this, "Lỗi khi đăng nhập: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        _btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("LoginActivity", "Register");
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
                            Log.d("LoginActivity", "UserClass: " + BackendlessUser.class);
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