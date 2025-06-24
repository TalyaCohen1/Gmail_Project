package com.example.android_app.ui.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_app.R;
import com.example.android_app.model.viewmodel.CreateMailViewModel;
import com.example.android_app.utils.ViewModelFactory;

public class CreateMailFragment extends Fragment {

    private EditText editTextTo, editTextSubject, editTextBody;
    private TextView textError;
    private Button buttonSend;
    private CreateMailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_mail, container, false);

        // חיבור לרכיבי UI
        editTextTo = view.findViewById(R.id.editTextTo);
        editTextSubject = view.findViewById(R.id.editTextSubject);
        editTextBody = view.findViewById(R.id.editTextBody);
        textError = view.findViewById(R.id.textError);
        buttonSend = view.findViewById(R.id.buttonSend);

        // יצירת ViewModel עם Factory (כדי לקבל Context)
        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(CreateMailViewModel.class);

        // תצפית על הודעות שגיאה
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                textError.setText(error);
                textError.setVisibility(View.VISIBLE);
            } else {
                textError.setVisibility(View.GONE);
            }
        });

        // כפתור שליחה
        buttonSend.setOnClickListener(v -> {
            String to = editTextTo.getText().toString();
            String subject = editTextSubject.getText().toString();
            String body = editTextBody.getText().toString();
            viewModel.sendEmail(to, subject, body);
        });

        return view;
    }
}
