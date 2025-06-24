package com.example.android_app.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.android_app.data.network.ApiClient;
import com.example.android_app.data.network.ApiService;
import com.example.android_app.model.Email;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_app.R;
import com.example.android_app.model.Email;

import java.util.Arrays;
import java.util.List;


public class EmailDetailsActivity extends AppCompatActivity {
    TextView senderView, subjectView, bodyView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_details);

        senderView = findViewById(R.id.emailSender);
        subjectView = findViewById(R.id.emailSubject);
        bodyView = findViewById(R.id.emailBody);

        String emailId = getIntent().getStringExtra("email_id");
        String userId = "123"; // או שלוף מ־SharedPreferences אם שמרת שם

        fetchEmailById(emailId, userId);
    }

    private void fetchEmailById(String emailId, String userId) {
        ApiClient.getApiService().getEmailById(emailId, userId).enqueue(new Callback<Email>() {
            @Override
            public void onResponse(Call<Email> call, Response<Email> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Email email = response.body();
                    senderView.setText("From: " + email.from);
                    subjectView.setText("Subject: " + email.subject);
                    bodyView.setText(email.body);
                } else {
                    senderView.setText("Email not found");
                    subjectView.setText("");
                    bodyView.setText("");
                }
            }

            @Override
            public void onFailure(Call<Email> call, Throwable t) {
                senderView.setText("Error loading email");
                subjectView.setText("");
                bodyView.setText("");
                Toast.makeText(EmailDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
