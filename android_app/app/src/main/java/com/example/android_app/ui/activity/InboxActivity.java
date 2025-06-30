package com.example.android_app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.android_app.R;
import com.example.android_app.model.Email;
import com.example.android_app.model.viewmodel.InboxViewModel;
import com.example.android_app.ui.EmailAdapter;
import com.example.android_app.ui.EmailDetailsActivity;
import com.example.android_app.ui.fragments.CreateMailFragment;
import com.example.android_app.utils.MailMapper;

import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends AppCompatActivity {

    private InboxViewModel viewModel;
    private EmailAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        recyclerView = findViewById(R.id.recyclerViewEmails);

        // קבלת מופע של ה-ViewModel
        viewModel = new ViewModelProvider(this).get(InboxViewModel.class);

        setupRecyclerView();
        setupRefreshListener();
        observeViewModel();

        // התחלת תהליך קבלת המיילים
        Log.d("MyDebug", "Activity onCreate: Calling viewModel.fetchInbox()");
        viewModel.fetchEmails();

        findViewById(R.id.fabCompose).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentCreateMailContainer, new CreateMailFragment())
                    .addToBackStack("compose")
                    .commit();

            // מציג את ה-FrameLayout
            findViewById(R.id.fragmentCreateMailContainer).setVisibility(View.VISIBLE);
        });
        //Refresh the inbox when the back stack changes
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().findFragmentById(R.id.fragmentCreateMailContainer) == null) {
                viewModel.fetchEmails();
            }
        });

    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewEmails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // 2. כשאנחנו יוצרים את האדפטר, אנחנו מעבירים לו 'this' בתור מאזין
        adapter = new EmailAdapter(this, new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }

    private void setupRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.fetchEmails();
        });
    }

    private void observeViewModel() {
        viewModel.getInboxEmails().observe(this, emails -> {
            // התצפית מתעדכנת כשהמידע מגיע
            if (emails != null) {
                List<Email> emailsToShow = MailMapper.toEmails(emails);
                adapter.setEmails(emailsToShow);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading == null) return;

            // אם המשתמש ביצע רענון בהחלקה, ה-SwipeRefreshLayout כבר מציג
            // אנימציה. לכן, אין צורך להציג גם את ה-ProgressBar הגדול.
            if (swipeRefreshLayout.isRefreshing()) {
                if (!isLoading) {
                    // אם הטעינה הסתיימה, פשוט נפסיק את אנימציית הרענון
                    swipeRefreshLayout.setRefreshing(false);
                }
            } else {
                // אם זו טעינה ראשונית (לא מהחלקה), נשתמש ב-ProgressBar המרכזי
                loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                // חשוב לוודא שכל אנימציות הטעינה מפסיקות גם במקרה של שגיאה
                swipeRefreshLayout.setRefreshing(false);
                loadingProgressBar.setVisibility(View.GONE);
            }
        });
    }

    public void onEmailClick(Email email) {
        Intent intent = new Intent(this, EmailDetailsActivity.class);
        intent.putExtra("EMAIL_ID", email.getId());
        startActivity(intent);
    }
}
