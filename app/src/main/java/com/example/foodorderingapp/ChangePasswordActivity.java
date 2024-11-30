package com.example.foodorderingapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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

public class ChangePasswordActivity extends BaseNetworkActivity {
    private EditText currentPasswordInput;
    private EditText newPasswordInput;
    private EditText confirmPasswordInput;
    private Button changePasswordButton;
    private Button backButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        currentPasswordInput = findViewById(R.id.current_password_input);
        newPasswordInput = findViewById(R.id.new_password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        changePasswordButton = findViewById(R.id.change_password_button);
        backButton = findViewById(R.id.back_button);
    }

    private void setupClickListeners() {
        changePasswordButton.setOnClickListener(v -> changePassword());
        backButton.setOnClickListener(v -> finish());
    }

    private void changePassword() {
        String currentPassword = currentPasswordInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate inputs
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Mật khẩu mới không khớp!");
            return;
        }

        if (newPassword.length() < 6) {
            showError("Mật khẩu mới phải có ít nhất 6 ký tự!");
            return;
        }

        showLoading(true);

        // Change password using Backendless
        Backendless.UserService.login(Backendless.UserService.CurrentUser().getEmail(),
                currentPassword, new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser user) {
                        // Current password is correct, proceed to change password
                        updatePassword(newPassword);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            showError("Mật khẩu hiện tại không đúng!");
                        });
                    }
                });
    }

    private void updatePassword(String newPassword) {
        Backendless.UserService.CurrentUser().setPassword(newPassword);
        Backendless.UserService.update(Backendless.UserService.CurrentUser(),
                new AsyncCallback<BackendlessUser>() {
                    @Override
                    public void handleResponse(BackendlessUser user) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            showMessage("Đổi mật khẩu thành công!");
                            finish();
                        });
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            showError("Lỗi đổi mật khẩu: " + fault.getMessage());
                        });
                    }
                });
    }

    private void showLoading(boolean show) {
        changePasswordButton.setEnabled(!show);
        backButton.setEnabled(!show);
        currentPasswordInput.setEnabled(!show);
        newPasswordInput.setEnabled(!show);
        confirmPasswordInput.setEnabled(!show);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}