package com.example.android_app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.example.android_app.model.viewmodel.InboxViewModel;


public class EmailDetailsActivity extends AppCompatActivity {
    TextView senderView, subjectView, bodyView;
    private EmailDetailsViewModel emailDetailsViewModel;
    private InboxViewModel inboxViewModel;
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

        LinearLayout sentButtonsGroup = findViewById(R.id.sentButtonsGroup);
        LinearLayout draftButtonsGroup = findViewById(R.id.draftButtonsGroup);

        sentButtonsGroup.setVisibility(View.GONE);
        draftButtonsGroup.setVisibility(View.GONE);

        if(!email.isSend()){
            //handle draft case
            draftButtonsGroup.setVisibility(View.VISIBLE);
            Button editDraftButton = findViewById(R.id.btnEditDraft);
            editDraftButton.setOnClickListener(v -> openCreateMail("edit", email));
        } else {
            //handle sent case
            sentButtonsGroup.setVisibility(View.VISIBLE);
            replyButton.setOnClickListener(v -> openCreateMail("replay", email));
            forwardButton.setOnClickListener(v -> openCreateMail("forward", email));
        }
    }


    private void openCreateMail(String type, Email email) {
        Bundle args = new Bundle();
        if(type.equals("replay")) {
            args.putString("to", email.getFrom());
            args.putString("subject",  email.getReplySubject());
            args.putString("body", email.getReplyBody());
        } else if(type.equals("forward")) {
            args.putString("to", email.getTo());
            args.putString("subject", "FWD: " + email.getForwardSubject());
            args.putString("body", email.getForwardBody());
        } else if(type.equals("edit")) {
            args.putString("to", email.getTo());
            args.putString("subject", email.getSubject());
            args.putString("body", email.getBody());
        }

        CreateMailFragment fragment = new CreateMailFragment();
        fragment.setArguments(args);
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);

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

