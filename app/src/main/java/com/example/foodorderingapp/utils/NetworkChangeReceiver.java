package com.example.foodorderingapp.utils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.example.foodorderingapp.DisconnectedActivity;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean isConnected = NetworkUtils.isNetworkConnected(context);
        if (!isConnected)
        {
            Toast.makeText(context, "Mất kết nối mạng!", Toast.LENGTH_SHORT).show();
            Intent disconnectedIntent = new Intent(context, DisconnectedActivity.class);
            disconnectedIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(disconnectedIntent);
        }
    }
}
