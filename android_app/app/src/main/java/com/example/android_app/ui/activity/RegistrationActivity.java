package com.example.android_app.ui.activity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.*;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Calendar;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_app.R;
import com.example.android_app.model.viewmodel.RegistrationViewModel;


public class RegistrationActivity extends AppCompatActivity {

    // Declare UI elements
    private EditText etFullName, etEmail, etPassword, etConfirmPassword, etBirthDate;
    private Calendar selectedBirthDate = Calendar.getInstance();
    private Spinner spinnerGender;
    private Button btnUploadImage, btnRegister;
    private ImageView imagePreview;
    // Declare variables
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize UI elements and connect them to their respective views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnRegister = findViewById(R.id.btnRegister);
        imagePreview = findViewById(R.id.imagePreview);
        etBirthDate = findViewById(R.id.etBirthDate);
        etBirthDate.setOnClickListener(v -> {
            final Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(this,R.style.CustomDatePickerDialog, (view, y, m, d) -> {
                selectedBirthDate.set(y, m, d);
                String selectedDate = String.format("%02d/%02d/%04d", d, m + 1, y);
                etBirthDate.setText(selectedDate);
            }, year, month, day);

            datePickerDialog.show();
        });


        //init spinner for gender
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        btnUploadImage = findViewById(R.id.btnUploadImage);
        //upload profile picture
        btnUploadImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 100); // requestCode = 100
        });

        RegistrationViewModel viewModel = new ViewModelProvider(this).get(RegistrationViewModel.class);
        viewModel.status.observe(this, message -> {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                if(message.equals("Registration successful")) {
                    Intent intent = new Intent(this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
        });

        btnRegister.setOnClickListener(v -> {
            if (validateForm()) {
                viewModel.registerUser(
                        etFullName.getText().toString().trim(),
                        etEmail.getText().toString().trim(),
                        getBirthDate(),
                        spinnerGender.getSelectedItem().toString(),
                        etPassword.getText().toString(),
                        selectedImageUri
                );
            }
        });
        TextView tvAlreadyHaveAccount = findViewById(R.id.tvAlreadyHaveAccount);
        tvAlreadyHaveAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    //validate form - check if all fields are legal
    private boolean validateForm() {
        boolean valid = true;

        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();

        //check if age is above 13
        int age = Calendar.getInstance().get(Calendar.YEAR) - selectedBirthDate.get(Calendar.YEAR);
        if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) < selectedBirthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        // check all fields
        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            valid = false;
        }
        if (!email.endsWith("@smail.com")) {
            etEmail.setError("Email must be @smail.com");
            valid = false;
        }
        if (age < 13) {
            Toast.makeText(this, "You must be at least 13 years old", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (!gender.equals("female") && !gender.equals("male")) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            valid = false;
        }
        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).{8,}$")) {
            etPassword.setError("Must be 8 chars, 1 uppercase, 1 digit");
            valid = false;
        }
        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            valid = false;
        }

        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            imagePreview.setImageURI(selectedImageUri);
        }
    }

    private String getBirthDate() {
        int year = selectedBirthDate.get(Calendar.YEAR);
        int month = selectedBirthDate.get(Calendar.MONTH) + 1; // 0-based
        int day = selectedBirthDate.get(Calendar.DAY_OF_MONTH);
        return String.format("%04d-%02d-%02d", year, month, day);
    }

}
