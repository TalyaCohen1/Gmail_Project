package com.example.android_app.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_app.R;
import com.example.android_app.model.Email;
import com.example.android_app.model.viewmodel.EmailDetailsViewModel;
import com.example.android_app.model.viewmodel.InboxViewModel;


public class EmailDetailsActivity extends AppCompatActivity {
    TextView senderView, subjectView, bodyView;
    private EmailDetailsViewModel emailDetailsViewModel;
    private InboxViewModel inboxViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_details);

        senderView = findViewById(R.id.emailSender);
        subjectView = findViewById(R.id.emailSubject);
        bodyView = findViewById(R.id.emailBody);

        String emailId = getIntent().getStringExtra("email_id");

        // אם אין ID, אין מה להמשיך
        if (emailId == null || emailId.isEmpty()) {
            Toast.makeText(this, "Error: Email ID not provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        emailDetailsViewModel = new ViewModelProvider(this).get(EmailDetailsViewModel.class);
        inboxViewModel = new ViewModelProvider(this).get(InboxViewModel.class);

        emailDetailsViewModel.fetchEmailById(emailId);
        Log.d("EmailDetailsActivity", "Attempting to mark email as read for ID: " + emailId);
        inboxViewModel.markEmailAsRead(emailId);

        setupObservers();
    }

    private void setupObservers() {
        // listen to valid data
        emailDetailsViewModel.getEmailDetails().observe(this, email -> {
            if (email != null) {
                updateUi(email);
            }
        });

        // listen to errors
        emailDetailsViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(EmailDetailsActivity.this, error, Toast.LENGTH_LONG).show();
                displayError();
            }
        });
    }

    private void updateUi(Email email) {
        senderView.setText("From: " + email.getFrom());
        subjectView.setText("Subject: " + email.getSubject());
        bodyView.setText(email.getBody());
    }

    private void displayError() {
        senderView.setText("Error loading email");
        subjectView.setText("");
        bodyView.setText("");
    }
}

//    private void fetchEmailById(String emailId, String userId) {
//        ApiClient.getApiService().getEmailById(emailId, userId).enqueue(new Callback<Email>() {
//            @Override
//            public void onResponse(Call<Email> call, Response<Email> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    Email email = response.body();
//                    senderView.setText("From: " + email.Sender);
//                    subjectView.setText("Subject: " + email.subject);
//                    bodyView.setText(email.body);
//                } else {
//                    senderView.setText("Email not found");
//                    subjectView.setText("");
//                    bodyView.setText("");
//                }
//            }
//
//            @Override
//            public void onFailure(Call<Email> call, Throwable t) {
//                senderView.setText("Error loading email");
//                subjectView.setText("");
//                bodyView.setText("");
//                Toast.makeText(EmailDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

