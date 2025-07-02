package com.example.android_app.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_app.R;
import com.example.android_app.model.viewmodel.CreateMailViewModel;
import com.example.android_app.utils.ViewModelFactory;
import com.example.android_app.model.Email;


public class CreateMailFragment extends Fragment {

    private EditText editTextTo, editTextSubject, editTextBody;
    private TextView textError;
    private Button buttonSend;
    private ImageButton buttonBack;
    private CreateMailViewModel viewModel;

    public static final String REQUEST_KEY_EMAIL_SENT = "email_sent_request_key";
    public static final String BUNDLE_KEY_EMAIL_SENT_SUCCESS = "email_sent_success";
    public static final String ARG_MAIL_ID = "mail_id"; // ארגומנט ל-ID של מייל (טיוטה קיימת)
    public static final String ARG_TO = "to";
    public static final String ARG_SUBJECT = "subject";
    public static final String ARG_BODY = "body";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_mail, container, false);

        // connect to ui elements
        editTextTo = view.findViewById(R.id.editTextTo);
        editTextSubject = view.findViewById(R.id.editTextSubject);
        editTextBody = view.findViewById(R.id.editTextBody);
        textError = view.findViewById(R.id.textError);
        buttonSend = view.findViewById(R.id.buttonSend);
        buttonBack = view.findViewById(R.id.buttonClose);

        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(CreateMailViewModel.class);

        // get draftID and initial values if exist
        String existingMailId = null;
        String defaultTo = "";
        String defaultSubject = "";
        String defaultBody = "";

        Bundle args = getArguments();
        if (args != null) {
            existingMailId = args.getString(ARG_MAIL_ID, null);
            defaultTo = args.getString(ARG_TO, ""); // השתמש בקבוע ARG_TO
            defaultSubject = args.getString(ARG_SUBJECT, ""); // השתמש בקבוע ARG_SUBJECT
            defaultBody = args.getString(ARG_BODY, ""); // השתמש בקבוע ARG_BODY

//            editTextTo.setText(defaultTo);
//            editTextSubject.setText(defaultSubject);
//            editTextBody.setText(defaultBody);
        }
        //load the draft if exist
        if (savedInstanceState == null) { // only once
            viewModel.loadOrCreateDraft(existingMailId, defaultTo, defaultSubject, defaultBody);
        }


        // *** צפה בטיוטה הנוכחית כדי לאכלס את ה-UI ***
        viewModel.getCurrentDraft().observe(getViewLifecycleOwner(), email -> {
            if (email != null) {
                // רק אם הטקסט בשדות שונה, עדכן אותו כדי למנוע קפיצות בזמן הקלדה
                if (!editTextTo.getText().toString().equals(email.getTo())) {
                    editTextTo.setText(email.getTo());
                }
                if (!editTextSubject.getText().toString().equals(email.getSubject())) {
                    editTextSubject.setText(email.getSubject());
                }
                if (!editTextBody.getText().toString().equals(email.getBody())) {
                    editTextBody.setText(email.getBody());
                }
            }
        });

        // צפה במצב טעינה/שמירה
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            editTextTo.setEnabled(!isLoading);
            editTextSubject.setEnabled(!isLoading);
            editTextBody.setEnabled(!isLoading);
            buttonSend.setEnabled(!isLoading);
            buttonBack.setEnabled(!isLoading);
//            if (!isLoading) {
//                requireActivity().findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
//            }
        });

        // צפה בהודעות שגיאה
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                textError.setText(error);
                textError.setVisibility(View.VISIBLE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show(); // משוב נוסף
            } else {
                textError.setVisibility(View.GONE);
            }
        });

        // צפה בסטטוס שליחת המייל
        viewModel.getEmailSent().observe(getViewLifecycleOwner(), sent -> {
            if (Boolean.TRUE.equals(sent)) {
                Bundle result = new Bundle();
                result.putBoolean(BUNDLE_KEY_EMAIL_SENT_SUCCESS, true);
                getParentFragmentManager().setFragmentResult(REQUEST_KEY_EMAIL_SENT, result);
                // סגירת הפרגמנט והחזרת התצוגה לאינבוקס
                requireActivity().getSupportFragmentManager().popBackStack();
                // Add null check here for fragmentCreateMailContainer
                View fragmentContainer = requireActivity().findViewById(R.id.fragment_container); // השתמש ב-fragment_container
                if (fragmentContainer != null) {
                    fragmentContainer.setVisibility(View.GONE);
                }
            }
        });

        viewModel.getActionSuccess().observe(getViewLifecycleOwner(), success -> {
        });

        // כפתור שליחה
        buttonSend.setOnClickListener(v -> {
            String to = editTextTo.getText().toString();
            String subject = editTextSubject.getText().toString();
            String body = editTextBody.getText().toString();
            viewModel.sendEmail(to, subject, body);
        });

        // חזרה לתיבת הדואר הנכנס + שמירת טיוטה
        buttonBack.setOnClickListener(v -> {
            saveDraftAndPopBack();
        });

        return view;
    }

    // מתודה לשמירת טיוטה לפני יציאה מהפרגמנט
    private void saveDraftAndPopBack() {
        String to = editTextTo.getText().toString();
        String subject = editTextSubject.getText().toString();
        String body = editTextBody.getText().toString();

        // שמור טיוטה רק אם יש תוכן כלשהו
        if (!TextUtils.isEmpty(to) || !TextUtils.isEmpty(subject) || !TextUtils.isEmpty(body)) {
            viewModel.saveDraft(to, subject, body);
        } else {
            // אם הטיוטה ריקה לחלוטין, ניתן לשקול למחוק אותה או לא לשמור כלל.
            // כרגע, אם היא ריקה, פשוט לא נקרא ל-saveDraft.
            Log.d("CreateMailFragment", "Draft is empty, not saving.");
        }

        requireActivity().getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onPause() {
        super.onPause();
        String to = editTextTo.getText().toString();
        String subject = editTextSubject.getText().toString();
        String body = editTextBody.getText().toString();

        if (!TextUtils.isEmpty(to) || !TextUtils.isEmpty(subject) || !TextUtils.isEmpty(body)) {
            viewModel.saveDraft(to, subject, body);
            Log.d("CreateMailFragment", "Draft saved on onPause.");
        } else {
            Log.d("CreateMailFragment", "Draft is empty, not saving on onPause.");
        }
    }
}