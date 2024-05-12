package com.example.toedy_project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.auth0.android.Auth0;
import com.auth0.android.authentication.AuthenticationAPIClient;
import com.auth0.android.authentication.AuthenticationException;
import com.auth0.android.callback.Callback;
import com.auth0.android.provider.WebAuthProvider;
import com.auth0.android.result.Credentials;
import com.auth0.android.result.UserProfile;

public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_CLEAR_CREDENTIALS = "com.auth0.CLEAR_CREDENTIALS";
    private Auth0 auth0;

    private Button loginButton;
    private Button offlineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginButton = findViewById(R.id.login_button_login);
        loginButton.setEnabled(false);
        loginButton.setOnClickListener(v -> login());

        offlineButton = findViewById(R.id.login_button_offline);
        offlineButton.setEnabled(false);
        offlineButton.setOnClickListener(v -> {
            GlobalStates states = (GlobalStates) getApplicationContext();
            states.userId = "";
            states.userName = "Offline User";
            states.auth0AccessToken = "";
            nextActivity();
        });

        auth0 = new Auth0(this);

        if (getIntent().getBooleanExtra(EXTRA_CLEAR_CREDENTIALS, false)) {
            logout();
            return;
        }

        check();
    }

    private void check() {
        String accessToken = getPreferences(0).getString("accessToken", "");
        AuthenticationAPIClient client = new AuthenticationAPIClient(auth0);
        client.userInfo(accessToken).start(new Callback<UserProfile, AuthenticationException>() {
            @Override
            public void onSuccess(UserProfile userProfile) {
                GlobalStates states = (GlobalStates) getApplicationContext();
                states.userId = userProfile.getId();
                states.userName = userProfile.getName();
                states.auth0AccessToken = accessToken;

                nextActivity();
            }

            @Override
            public void onFailure(@NonNull AuthenticationException e) {
                loginButton.setEnabled(true);
                offlineButton.setEnabled(true);
            }
        });
    }

    private void login() {
        WebAuthProvider.login(auth0)
                .withScheme("demo")
                .withAudience(String.format("https://%s/userinfo", getString(R.string.com_auth0_domain)))
                .start(this, new Callback<Credentials, AuthenticationException>() {
                    @Override
                    public void onFailure(@NonNull final AuthenticationException exception) {
                        Toast.makeText(LoginActivity.this,
                                "Error: " + exception.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onSuccess(@Nullable final Credentials credentials) {
                        assert credentials != null;
                        GlobalStates states = (GlobalStates) getApplicationContext();
                        states.userId = credentials.getUser().getId();
                        states.userName = credentials.getUser().getName();
                        states.auth0AccessToken = credentials.getAccessToken();
                        getPreferences(0).edit()
                                .putString("accessToken", states.auth0AccessToken).apply();

                        nextActivity();
                    }
                });
    }

    private void logout() {
        GlobalStates states = (GlobalStates) getApplicationContext();
        if (states.userId == null || states.userId.isEmpty()) {
            loginButton.setEnabled(true);
            offlineButton.setEnabled(true);
            states.userName = "";
            states.auth0AccessToken = "";
            return;
        }

        WebAuthProvider.logout(auth0)
                .withScheme("demo")
                .start(this, new Callback<Void, AuthenticationException>() {
                    @Override
                    public void onSuccess(@Nullable Void payload) {
                        loginButton.setEnabled(true);
                        offlineButton.setEnabled(true);

                        GlobalStates states = (GlobalStates) getApplicationContext();
                        states.userId = null;
                        states.userName = null;
                        states.auth0AccessToken = null;
                        getPreferences(0).edit()
                                .putString("accessToken", "").apply();
                    }

                    @Override
                    public void onFailure(@NonNull AuthenticationException error) {
                        nextActivity();
                    }
                });
    }

    private void nextActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}