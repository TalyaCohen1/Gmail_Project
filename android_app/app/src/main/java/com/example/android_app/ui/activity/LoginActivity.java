package com.example.android_app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_app.R;
import com.example.android_app.model.viewmodel.LoginViewModel;
import com.example.android_app.utils.SharedPrefsManager;


public class LoginActivity extends AppCompatActivity {
    private EditText emailField, passwordField;
    private TextView errorText;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailField = findViewById(R.id.etEmail);
        passwordField = findViewById(R.id.etPassword);
        errorText = findViewById(R.id.errorMessage);

        Button loginButton = findViewById(R.id.btnLogin);
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        loginButton.setOnClickListener(v -> {
            String email = emailField.getText().toString().trim();
            String password = passwordField.getText().toString().trim();
            viewModel.login(email, password);
        });

        viewModel.getLoginResult().observe(this, result -> {
            Toast.makeText(this, "Welcome " + result.getFullName(), Toast.LENGTH_LONG).show();
            SharedPrefsManager.save(this, "token", result.getToken());
            SharedPrefsManager.save(this, "fullName", result.getFullName());
            SharedPrefsManager.save(this, "profileImage", result.getProfileImage() != null ? result.getProfileImage() : "/uploads/default-profile.png");
            SharedPrefsManager.save(this, "userId", result.getUserId());


            Intent intent = new Intent(this, InboxActivity.class);
            startActivity(intent);
            finish();
        });

        viewModel.getErrorMessage().observe(this, error -> {
            TextView errorView = findViewById(R.id.errorMessage);
            errorView.setText(error);
        });
    }
}
