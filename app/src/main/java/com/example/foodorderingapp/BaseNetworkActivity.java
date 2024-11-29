package com.example.foodorderingapp;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import androidx.appcompat.app.AppCompatActivity;
import com.example.foodorderingapp.utils.NetworkChangeReceiver;
import com.example.foodorderingapp.utils.NetworkUtils;

public abstract class BaseNetworkActivity extends AppCompatActivity {
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onStart() {
        super.onStart();
        if (!NetworkUtils.isNetworkConnected(this)) {
            navigateToDisconnected();
            return;
        }
        setupNetworkReceiver();
    }

    private void setupNetworkReceiver() {
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
    }

    protected void navigateToDisconnected() {
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