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
import com.example.foodorderingapp.utils.NetworkChangeReceiver;
import com.example.foodorderingapp.utils.NetworkUtils;

public class HomeActivity extends AppCompatActivity {

    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        /*
         * Backendless.initApp( getApplicationContext(),
         * Environment.APPLICATION_ID,
         * Environment.API_KEY );
         */

        networkChangeReceiver = new NetworkChangeReceiver();

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);

        if (!NetworkUtils.isNetworkConnected(this)) {
            Intent intent = new Intent(this, DisconnectedActivity.class);
            startActivity(intent);
            finish();
        }

        // setContentView(R.layout.activity_main);
        Backendless.initApp(getApplicationContext(), Environment.APPLICATION_ID, Environment.API_KEY);

        Intent intent = new Intent(this, ProductActivity.class);
        startActivity(intent);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
    }
}