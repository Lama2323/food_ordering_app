package com.example.foodorderingapp;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.example.foodorderingapp.utils.NetworkChangeReceiver;
import com.example.foodorderingapp.utils.NetworkUtils;

public class HomeActivity extends AppCompatActivity {

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Khởi tạo Backendless
        Backendless.initApp(getApplicationContext(), Environment.APPLICATION_ID, Environment.API_KEY);

        // Thiết lập network receiver
        setupNetworkReceiver();

        // Kiểm tra kết nối mạng
        if (!NetworkUtils.isNetworkConnected(this)) {
            navigateToDisconnected();
            return;
        }

        // Lấy current user
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();

        // Điều hướng dựa trên trạng thái user
        Intent intent = new Intent(this, (currentUser == null) ? LoginActivity.class : ProductActivity.class);
        startActivity(intent);
        finish();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupNetworkReceiver() {
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    private void navigateToDisconnected() {
        Intent intent = new Intent(this, DisconnectedActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}