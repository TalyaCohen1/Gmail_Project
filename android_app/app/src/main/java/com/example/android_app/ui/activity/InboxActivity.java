package com.example.android_app.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.R;
import com.example.android_app.model.Email;
import com.example.android_app.model.viewmodel.InboxViewModel;
import com.example.android_app.ui.EmailAdapter;

import java.util.ArrayList;

public class InboxActivity extends AppCompatActivity implements EmailAdapter.OnEmailClickListener {

    private InboxViewModel viewModel;
    private EmailAdapter adapter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        // קבלת מופע של ה-ViewModel
        viewModel = new ViewModelProvider(this).get(InboxViewModel.class);

        setupRecyclerView();
        observeViewModel();

        // התחלת תהליך קבלת המיילים
        viewModel.fetchEmails();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewEmails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 2. כשאנחנו יוצרים את האדפטר, אנחנו מעבירים לו 'this' בתור מאזין
        adapter = new EmailAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);
    }

    private void observeViewModel() {
        viewModel.getEmails().observe(this, emails -> {
            // התצפית מתעדכנת כשהמידע מגיע
            if (emails != null) {
                adapter.updateEmails(emails);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            // הצג/הסתר אנימציית טעינה
        });
    }

    // 3. מימוש הפונקציה מה-interface
    @Override
    public void onEmailClick(Email email) {
        Intent intent = new Intent(this, EmailDetailActivity.class);
        intent.putExtra("EMAIL_ID", email.getId());
        startActivity(intent);
    }
}
