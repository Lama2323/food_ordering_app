package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

public class MainActivity extends BaseAuthenticatedActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Backendless.initApp(getApplicationContext(), Environment.APPLICATION_ID, Environment.API_KEY);
    }

    @Override
    protected void onAuthenticated() {
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();
        Intent intent = new Intent(this, (currentUser == null) ? LoginActivity.class : ProductActivity.class);
        startActivity(intent);
        finish();
    }
}