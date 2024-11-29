package com.example.foodorderingapp;

import android.content.Intent;
import android.os.Bundle;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;

public abstract class BaseAuthenticatedActivity extends BaseNetworkActivity {
    private static final String TAG = "BaseAuthActivity";
    protected BackendlessUser currentUser;
    private boolean isAuthenticationChecked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAuthentication();
    }

    private void checkAuthentication() {
        if (isAuthenticationChecked) return;

        currentUser = Backendless.UserService.CurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
            @Override
            public void handleResponse(Boolean isValid) {
                if (isValid) {
                    isAuthenticationChecked = true;
                    runOnUiThread(() -> {
                        try {
                            onAuthenticated();
                        } catch (Exception e) {
                            handleAuthenticationError(e);
                        }
                    });
                } else {
                    handleInvalidSession();
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                handleInvalidSession();
            }
        });
    }

    private void handleInvalidSession() {
        Backendless.UserService.logout(new AsyncCallback<Void>() {
            @Override
            public void handleResponse(Void response) {
                redirectToLogin();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                redirectToLogin();
            }
        });
    }

    protected void handleAuthenticationError(Exception e) {
        runOnUiThread(this::redirectToLogin);
    }

    private void redirectToLogin() {
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