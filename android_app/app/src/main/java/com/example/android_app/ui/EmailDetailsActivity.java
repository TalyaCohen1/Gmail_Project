package com.example.android_app.ui;

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

        // אם אין ID, אין מה להמשיך
        if (emailId == null || emailId.isEmpty()) {
            Toast.makeText(this, "Error: Email ID not provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        emailDetailsViewModel = new ViewModelProvider(this).get(EmailDetailsViewModel.class);
        inboxViewModel = new ViewModelProvider(this).get(InboxViewModel.class);

        emailDetailsViewModel.fetchEmailById(emailId);
        emailDetailsViewModel.fetchAllLabels();
        Log.d("EmailDetailsActivity", "Attempting to mark email as read for ID: " + emailId);
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
        senderView.setText(email.getSenderName());
        subjectView.setText(email.getSubject());
        bodyView.setText(email.getBody());

        if (email.getProfilePicUrl() != null && !email.getProfilePicUrl().isEmpty()) {
            Glide.with(this)
                    .load(email.getProfilePicUrl())
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop() // לעיגול התמונה
                    .into(senderProfileImageView);
        } else {
            senderProfileImageView.setImageResource(R.drawable.ic_profile_placeholder);
        }

        if (email.getDate() != null) {
            // פורמט לדוגמה: "Jun 29, 2025 10:30 AM"
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            emailTimeView.setText(sdf.format(email.getDate()));
        } else {
            emailTimeView.setText(""); // או טקסט ברירת מחדל
        }

        // כפתור חשוב
        updateImportantButton(email.isImportant());
        btnImportant.setOnClickListener(v -> {
            emailDetailsViewModel.toggleMailImportant(email.getId(), !email.isImportant());
        });

        // כפתור כוכב
        updateStarredButton(email.isStarred());
        btnStar.setOnClickListener(v -> {
            emailDetailsViewModel.toggleMailStarred(email.getId(), !email.isStarred());
        });

        // כפתור נקרא/לא נקרא
        updateReadUnreadButton(email.isRead());
        btnMarkAsReadUnread.setOnClickListener(v -> {
            emailDetailsViewModel.toggleMailReadStatus(email.getId(), !email.isRead());
        });

        updateSpamButton(email.isSpam());
        btnSpam.setOnClickListener(v -> {
            // הפיכת המצב הנוכחי של ספאם
            boolean newSpamStatus = !email.isSpam();
            emailDetailsViewModel.toggleMailSpam(email.getId(), newSpamStatus);
            // אם המייל סומן כספאם, נחזור אחורה. אם הוסר מספאם, נשאר במסך.
            if (newSpamStatus) {
                Toast.makeText(this, "המייל סומן כספאם והועבר", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "המייל הוסר מספאם", Toast.LENGTH_SHORT).show();
            }
        });

        // כפתור מחיקה
        String currentCategory = inboxViewModel.getCurrentCategoryOrLabelId();
        if (currentCategory != null && currentCategory.equals("trash")) {
            btnDelete.setVisibility(View.GONE); // הסתר את כפתור המחיקה
        } else {
            btnDelete.setVisibility(View.VISIBLE); // הצג את כפתור המחיקה
            btnDelete.setOnClickListener(v -> {
                emailDetailsViewModel.deleteMail(email.getId());
                Toast.makeText(this, "המייל נמחק", Toast.LENGTH_SHORT).show();
                finish(); // חזור לפעילות הקודמת לאחר המחיקה
            });
        }

//        // כפתור לייבלים (יישום עתידי)
//        btnLabels.setOnClickListener(v -> {
//            Toast.makeText(this, "פונקציונליות לייבלים טרם יושמה", Toast.LENGTH_SHORT).show();
//        });


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

    private void updateImportantButton(boolean isImportant) {
        if (isImportant) {
            btnImportant.setImageResource(R.drawable.full_important); // צרי קובץ important_on.xml/png ב-res/drawable
        } else {
            btnImportant.setImageResource(R.drawable.important); // צרי קובץ important_off.xml/png ב-res/drawable
        }
    }

    private void updateStarredButton(boolean isStarred) {
        if (isStarred) {
            btnStar.setImageResource(R.drawable.full_star); // צרי קובץ star_on.xml/png ב-res/drawable
        } else {
            btnStar.setImageResource(R.drawable.starred); // צרי קובץ star_off.xml/png ב-res/drawable
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
        // וודא שיש לנו את ה-emailId של המייל הנוכחי
        String emailId = getIntent().getStringExtra("email_id");
        if (emailId == null || emailId.isEmpty()) {
            Toast.makeText(this, "error: cannot load labels without emailId", Toast.LENGTH_SHORT).show();
            return;
        }

        // קבל את רשימת הלייבלים הזמינים מה-ViewModel הנכון!
        // השתמש ב-emailDetailsViewModel.getAllLabels()
        emailDetailsViewModel.getAllLabels().observe(this, labels -> { // *** התיקון כאן ***
            if (labels != null) { // אין צורך ב-!labels.isEmpty() כאן, נטפל בזה בפנים
                if (labels.isEmpty()) {
                    Toast.makeText(EmailDetailsActivity.this, "labels not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                PopupMenu popup = new PopupMenu(EmailDetailsActivity.this, anchorView);
                // הקודים 'isEmpty()', 'size()', 'get(int)' עובדים על אובייקט מסוג List<Label>
                // ולכן הבעיות הללו ייפתרו ברגע שהטיפוס יהיה נכון (כמו עכשיו)
                for (int i = 0; i < labels.size(); i++) {
                    popup.getMenu().add(0, i, i, labels.get(i).getName()); // השתמש בשם הלייבל
                }

                popup.setOnMenuItemClickListener(item -> {
                    // כאשר נבחר לייבל מהתפריט הראשון
                    Label selectedLabel = labels.get(item.getItemId()); // קבל את אובייקט הלייבל שנבחר
                    showAddRemoveMenu(anchorView, emailId, selectedLabel);
                    return true;
                });
                popup.show();
            } else {
                // מקרה שבו ה-LiveData מחזיר null, למרות ש-postValue בדרך כלל מונע זאת
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

