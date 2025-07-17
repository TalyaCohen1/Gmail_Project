package com.example.android_app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.android_app.R;
import com.example.android_app.model.Email;
import com.example.android_app.model.Label;
import com.example.android_app.model.viewmodel.EmailDetailsViewModel;
import com.example.android_app.model.viewmodel.InboxViewModel;
import com.example.android_app.ui.fragments.CreateMailFragment;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class EmailDetailsActivity extends AppCompatActivity {
    TextView senderView, subjectView, bodyView, emailTimeView;
    ImageView senderProfileImageView;
    ImageButton btnBack, btnImportant, btnSpam, btnDelete, btnMarkAsReadUnread, btnLabels, btnStar;
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

        senderProfileImageView = findViewById(R.id.senderProfileImage);
        emailTimeView = findViewById(R.id.emailTime);
        btnBack = findViewById(R.id.btnBack);
        btnImportant = findViewById(R.id.btnImportant);
        btnSpam = findViewById(R.id.btnSpam);
        btnDelete = findViewById(R.id.btnDelete);
        btnMarkAsReadUnread = findViewById(R.id.btnMarkAsReadUnread);
        btnLabels = findViewById(R.id.btnLabels);
        btnStar = findViewById(R.id.btnStar);

        btnBack.setOnClickListener(v -> finish());
        btnLabels.setOnClickListener(v -> {
            showLabelsMenu(v);
        });

        String emailId = getIntent().getStringExtra("email_id");

        emailDetailsViewModel = new ViewModelProvider(this).get(EmailDetailsViewModel.class);
        inboxViewModel = new ViewModelProvider(this).get(InboxViewModel.class);

        emailDetailsViewModel.fetchEmailById(emailId);
        emailDetailsViewModel.fetchAllLabels();
        inboxViewModel.markEmailAsRead(emailId);

        setupObservers();

        //listen to change in create mail fragment - so we refresh page if new mail is send
        getSupportFragmentManager().setFragmentResultListener(
                CreateMailFragment.REQUEST_KEY_EMAIL_SENT,
                this, // LifecycleOwner
                (requestKey, result) -> {
                    if (requestKey.equals(CreateMailFragment.REQUEST_KEY_EMAIL_SENT)) {
                        boolean emailSentSuccess = result.getBoolean(CreateMailFragment.BUNDLE_KEY_EMAIL_SENT_SUCCESS, false);
                        if (emailSentSuccess) {
                            //bring back to inbox the result of sending mail
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("email_sent_from_details", true);
                            setResult(Activity.RESULT_OK, resultIntent);

                            getSupportFragmentManager().popBackStack();
                            View fragmentContainer = findViewById(R.id.fragment_container);
                            if (fragmentContainer != null) {
                                fragmentContainer.setVisibility(View.GONE);
                            }
                            finish(); //back to inbox
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
        senderView.setText(email.getSenderName());
        subjectView.setText(email.getSubject());
        bodyView.setText(email.getBody());

        if (email.getProfilePicUrl() != null && !email.getProfilePicUrl().isEmpty()) {
            Glide.with(this)
                    .load(email.getProfilePicUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(senderProfileImageView);
        } else {
            senderProfileImageView.setImageResource(R.drawable.ic_profile_placeholder);
        }

        if (email.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            emailTimeView.setText(sdf.format(email.getDate()));
        } else {
            emailTimeView.setText("");
        }
        //important button
        updateImportantButton(email.isImportant());
        btnImportant.setOnClickListener(v -> {
            emailDetailsViewModel.toggleMailImportant(email.getId(), !email.isImportant());
        });
        //star button
        updateStarredButton(email.isStarred());
        btnStar.setOnClickListener(v -> {
            emailDetailsViewModel.toggleMailStarred(email.getId(), !email.isStarred());
        });

        // read/unread button
        updateReadUnreadButton(email.isRead());
        btnMarkAsReadUnread.setOnClickListener(v -> {
            emailDetailsViewModel.toggleMailReadStatus(email.getId(), !email.isRead());
        });

        updateSpamButton(email.isSpam());
        btnSpam.setOnClickListener(v -> {
            boolean newSpamStatus = !email.isSpam();
            emailDetailsViewModel.toggleMailSpam(email.getId(), newSpamStatus);
            if (newSpamStatus) {
                finish();
            }
        });
        //delete button
        String currentCategory = inboxViewModel.getCurrentCategoryOrLabelId();
        if (currentCategory != null && currentCategory.equals("trash")) {
            btnDelete.setVisibility(View.GONE);
        } else {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> {
                emailDetailsViewModel.deleteMail(email.getId());
            });
        }

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
            args.putString(CreateMailFragment.ARG_TO, "");
            args.putString(CreateMailFragment.ARG_SUBJECT, email.getForwardSubject());
            args.putString(CreateMailFragment.ARG_BODY, email.getForwardBody());
        } else if(type.equals("edit")) {
            args.putString(CreateMailFragment.ARG_TO, email.getTo());
            args.putString(CreateMailFragment.ARG_SUBJECT, email.getSubject());
            args.putString(CreateMailFragment.ARG_BODY, email.getBody());
            args.putString(CreateMailFragment.ARG_MAIL_ID, email.getId());
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

    private void updateImportantButton(boolean isImportant) {
        if (isImportant) {
            btnImportant.setImageResource(R.drawable.full_important);
        } else {
            btnImportant.setImageResource(R.drawable.important);
        }
    }

    private void updateStarredButton(boolean isStarred) {
        if (isStarred) {
            btnStar.setImageResource(R.drawable.full_star);
        } else {
            btnStar.setImageResource(R.drawable.starred);
        }
    }

    private void updateReadUnreadButton(boolean isRead) {
        if (isRead) {
            btnMarkAsReadUnread.setImageResource(R.drawable.mark_as_unread);
        } else {
            btnMarkAsReadUnread.setImageResource(R.drawable.mark_as_read);
        }
    }

    private void updateSpamButton(boolean isSpam) {
        if (isSpam) {
            btnSpam.setImageResource(R.drawable.spam_on);
            btnSpam.setContentDescription("unmark as spam");
        } else {
            btnSpam.setImageResource(R.drawable.spam);
            btnSpam.setContentDescription("mark as spam");
        }
    }

    private void showLabelsMenu(View anchorView) {
        String emailId = getIntent().getStringExtra("email_id");
        if (emailId == null || emailId.isEmpty()) {
            Toast.makeText(this, "error: cannot load labels without emailId", Toast.LENGTH_SHORT).show();
            return;
        }

        emailDetailsViewModel.getAllLabels().observe(this, labels -> {
            if (labels != null) {
                if (labels.isEmpty()) {
                    Toast.makeText(EmailDetailsActivity.this, "labels not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                PopupMenu popup = new PopupMenu(EmailDetailsActivity.this, anchorView);

                for (int i = 0; i < labels.size(); i++) {
                    popup.getMenu().add(0, i, i, labels.get(i).getName());
                }

                popup.setOnMenuItemClickListener(item -> {
                    Label selectedLabel = labels.get(item.getItemId());
                    showAddRemoveMenu(anchorView, emailId, selectedLabel);
                    return true;
                });
                popup.show();
            } else {
                Toast.makeText(EmailDetailsActivity.this, "error loading labels", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddRemoveMenu(View anchorView, String emailId, Label label) {
        PopupMenu popup = new PopupMenu(EmailDetailsActivity.this, anchorView);
        popup.getMenu().add("add to label");
        popup.getMenu().add("remove from label");

        popup.setOnMenuItemClickListener(item -> {
            String title = item.getTitle().toString();
            if ("add to label".equals(title)) {
                emailDetailsViewModel.addMailToLabel(emailId, label.getId());
                Toast.makeText(EmailDetailsActivity.this, "added to label" + label.getName(), Toast.LENGTH_SHORT).show();
            } else if ("remove from label".equals(title)) {
                emailDetailsViewModel.removeMailFromLabel(emailId, label.getId());
                Toast.makeText(EmailDetailsActivity.this, "removed from label" + label.getName(), Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popup.show();
    }


    @Override
    public void onBackPressed() {
        //back to inbox
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
            View fragmentContainer = findViewById(R.id.fragment_container);
            if (fragmentContainer != null) {
                fragmentContainer.setVisibility(View.GONE);
            }
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                finish();
            }
        } else {
            super.onBackPressed();
        }
    }
}