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
    private Email currentEmail;

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
        inboxViewModel.markEmailAsRead(emailId);

        setupObservers();

        // *** שינוי חדש: האזנה לתוצאות מ-CreateMailFragment ***
        getSupportFragmentManager().setFragmentResultListener(
                CreateMailFragment.REQUEST_KEY_EMAIL_SENT,
                this, // LifecycleOwner
                (requestKey, result) -> {
                    if (requestKey.equals(CreateMailFragment.REQUEST_KEY_EMAIL_SENT)) {
                        boolean emailSentSuccess = result.getBoolean(CreateMailFragment.BUNDLE_KEY_EMAIL_SENT_SUCCESS, false);
                        if (emailSentSuccess) {
                            // המייל נשלח בהצלחה מ-CreateMailFragment, לכן נסיים את הפעילות הנוכחית
                            finish(); // חזרה לאינבוקס
                        }
                    }
                }
        );
    }

    private void setupObservers() {
        // listen to valid data
        emailDetailsViewModel.getEmailDetails().observe(this, email -> {
            if (email != null) {
                this.currentEmail = email;
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

        if (sentButtonsGroup != null) sentButtonsGroup.setVisibility(View.GONE);
        if (draftButtonsGroup != null) draftButtonsGroup.setVisibility(View.GONE);

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
        if(email == null) {
            Toast.makeText(this, "Email data not available.", Toast.LENGTH_SHORT).show();
            return;
        }
        View fragmentContainer = findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        }

        Bundle args = new Bundle();
        if(type.equals("replay")) {
            args.putString(CreateMailFragment.ARG_TO, email.getFrom());
            args.putString(CreateMailFragment.ARG_SUBJECT,  email.getReplySubject());
            args.putString(CreateMailFragment.ARG_BODY, email.getReplyBody());
        } else if(type.equals("forward")) {
            args.putString(CreateMailFragment.ARG_TO, ""); // לרוב בהעברה השדה "אל" ריק
            args.putString(CreateMailFragment.ARG_SUBJECT, email.getForwardSubject());
            args.putString(CreateMailFragment.ARG_BODY, email.getForwardBody());
        } else if(type.equals("edit")) {
            args.putString(CreateMailFragment.ARG_TO, email.getTo());
            args.putString(CreateMailFragment.ARG_SUBJECT, email.getSubject());
            args.putString(CreateMailFragment.ARG_BODY, email.getBody());
            args.putString(CreateMailFragment.ARG_MAIL_ID, email.getId()); // *** תיקון 3: שימוש בקבוע ***
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

    @Override
    public void onBackPressed() {
        // *** שינוי חדש: טיפול בחזרה לאינבוקס לאחר סגירת הפרגמנט ***
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // אם יש פרגמנטים במחסנית (כלומר, CreateMailFragment פתוח), נוציא אותו
            getSupportFragmentManager().popBackStack();
            View fragmentContainer = findViewById(R.id.fragment_container);
            if (fragmentContainer != null) {
                fragmentContainer.setVisibility(View.GONE); // נסתיר את הקונטיינר
            }
            // לאחר הוצאת הפרגמנט, אם אין יותר פרגמנטים, נסיים את הפעילות הנוכחית
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                finish(); // חזרה לאינבוקס
            }
        } else {
            // אם אין פרגמנטים במחסנית, נבצע את פעולת ברירת המחדל של כפתור החזרה (שתחזיר לאינבוקס)
            super.onBackPressed();
        }
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

