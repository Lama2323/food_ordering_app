package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public abstract class BaseAuthenticatedActivity extends AppCompatActivity {
    private static final String TAG = "BaseAuthActivity";
    protected BackendlessUser currentUser;
    private boolean isAuthenticationChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activity con sẽ gọi setContentView riêng trong onCreate của nó
        checkAuthentication();
    }

    private void checkAuthentication() {
        if (isAuthenticationChecked) {
            return;
        }

        currentUser = Backendless.UserService.CurrentUser();

        if (currentUser == null) {
            Log.d(TAG, "No current user found, redirecting to login");
            redirectToLogin();
            return;
        }

        Log.d(TAG, "Verifying session validity...");
        Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
            @Override
            public void handleResponse(Boolean isValid) {
                if (isValid) {
                    Log.d(TAG, "Session is valid, proceeding with authentication");
                    isAuthenticationChecked = true;
                    runOnUiThread(() -> {
                        try {
                            onAuthenticated();
                        } catch (Exception e) {
                            Log.e(TAG, "Error in onAuthenticated: " + e.getMessage());
                            handleAuthenticationError(e);
                        }
                    });
                } else {
                    Log.d(TAG, "Session is invalid, handling invalid session");
                    handleInvalidSession();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Session check error: " + fault.getMessage());
                handleInvalidSession();
            }
        });
    }

    private void handleInvalidSession() {
        Log.d(TAG, "Logging out user due to invalid session");
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                Log.i(TAG, "User logged out successfully");
                redirectToLogin();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Logout error: " + fault.getMessage());
                redirectToLogin();
            }
        });
    }

    protected void handleAuthenticationError(Exception e) {
        Log.e(TAG, "Authentication error occurred", e);
        runOnUiThread(() -> {
            // Override this in child activities if needed
            redirectToLogin();
        });
    }

    private void redirectToLogin() {
        Log.d(TAG, "Redirecting to login activity");
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("fromActivity", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    protected abstract void onAuthenticated();

    protected String getUserProperty(String propertyName) {
        if (currentUser != null && currentUser.getProperty(propertyName) != null) {
            return currentUser.getProperty(propertyName).toString();
        }
        return "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isAuthenticationChecked) {
            checkAuthentication();
        }
    }
}