package com.example.foodorderingapp;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.foodorderingapp.utils.NetworkUtils;

public class DisconnectedActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disconnected);

        findViewById(R.id.btn_retry).setOnClickListener(view -> {
            if (NetworkUtils.isNetworkConnected(this)) {
                finish();
            } else {
                Toast.makeText(this, "Mất kết nối mạng! Vui lòng kiểm tra lại kết nối.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
    }
}