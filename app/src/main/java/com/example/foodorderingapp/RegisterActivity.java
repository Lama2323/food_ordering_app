package com.example.foodorderingapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class RegisterActivity extends BaseNetworkActivity {

    private EditText _txtEmail, _txtPassword, _txtReEnterPassword;
    private Button _btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        _txtEmail = findViewById(R.id.txt_RegisterEmailAddress);
        _txtPassword = findViewById(R.id.txt_RegisterPassword);
        _txtReEnterPassword = findViewById(R.id.txt_RegisterReEnterPassword);
        _btnRegister = findViewById(R.id.btn_RegisterUser);

        //Log.v("RegisterActivity", "before check");

        _btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(_txtEmail.getText().toString().isEmpty() || _txtPassword.getText().toString().isEmpty()
                        || _txtReEnterPassword.getText().toString().isEmpty())
                {
                    Toast.makeText(RegisterActivity.this,"Hãy nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (_txtPassword.getText().toString().trim().equals(_txtReEnterPassword.getText().toString().trim()))
                    {
                        Log.v("RegisterActivity", "before add");
                        String email = _txtEmail.getText().toString().trim();
                        String password = _txtPassword.getText().toString().trim();
                        BackendlessUser user = new BackendlessUser();
                        user.setEmail(email);
                        user.setPassword(password);
                        Backendless.UserService.register(user, new AsyncCallback<BackendlessUser>() {
                            @Override
                            public void handleResponse(BackendlessUser response) {
                                Log.v("RegisterActivity", "add user success");
                                Toast.makeText(RegisterActivity.this,"Thêm mới người dùng thành công", Toast.LENGTH_SHORT).show();
                                RegisterActivity.this.finish();
                            }

                            @Override
                            public void handleFault(BackendlessFault fault) {
                                Log.v("RegisterActivity", "add user failed");
                                Toast.makeText(RegisterActivity.this,"Không thể thêm người dùng, lỗi: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else
                    {
                        Toast.makeText(RegisterActivity.this,"Mật khẩu nhập không khớp với mật khẩu phía trên", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}