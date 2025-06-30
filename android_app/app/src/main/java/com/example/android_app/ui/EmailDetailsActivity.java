package com.example.android_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.fragment.app.FragmentTransaction;


import com.example.android_app.R;
import com.example.android_app.model.Email;
import com.example.android_app.model.viewmodel.EmailDetailsViewModel;
import com.example.android_app.ui.fragments.CreateMailFragment;


public class EmailDetailsActivity extends AppCompatActivity {
    TextView senderView, subjectView, bodyView;
    private EmailDetailsViewModel viewModel;
    Button replyButton, forwardButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_details);

        senderView = findViewById(R.id.emailSender);
        subjectView = findViewById(R.id.emailSubject);
        bodyView = findViewById(R.id.emailBody);
        replyButton = findViewById(R.id.btnReply);
        forwardButton = findViewById(R.id.btnForward);

        String emailId = getIntent().getStringExtra("email_id");

        // אם אין ID, אין מה להמשיך
        if (emailId == null || emailId.isEmpty()) {
            Toast.makeText(this, "Error: Email ID not provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(EmailDetailsViewModel.class);

        viewModel.fetchEmailById(emailId);

        setupObservers();
    }

    private void setupObservers() {
        // listen to valid data
        viewModel.getEmailDetails().observe(this, email -> {
            if (email != null) {
                updateUi(email);
            }
        });

        // listen to errors
        viewModel.getError().observe(this, error -> {
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
        replyButton.setOnClickListener(v -> openCreateMail("replay", email));
        forwardButton.setOnClickListener(v -> openCreateMail("forward", email));
    }

    private void openCreateMail(String type, Email email) {
        Bundle args = new Bundle();
        if(type.equals("replay")) {
            args.putString("to", email.getFrom());
            args.putString("subject",  email.getReplySubject());
        } else if(type.equals("forward")) {
            args.putString("to", email.getTo());
            args.putString("subject", "FWD: " + email.getForwardSubject());
        }

        CreateMailFragment fragment = new CreateMailFragment();
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
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

