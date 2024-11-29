package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

public class HomeActivity extends BaseAuthenticatedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Backendless.initApp(getApplicationContext(), Environment.APPLICATION_ID, Environment.API_KEY);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onAuthenticated() {
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        Intent intent = new Intent(this, (currentUser == null) ? LoginActivity.class : ProductActivity.class);
        startActivity(intent);
        finish();
    }
}