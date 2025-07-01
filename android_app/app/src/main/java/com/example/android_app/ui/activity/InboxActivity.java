package com.example.android_app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent; // Added import for KeyEvent
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText; // Added import for EditText
import android.widget.ImageView; // Added import for ImageView
import android.widget.ProgressBar;
import android.widget.TextView; // Added import for TextView

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Added import for Toolbar
import androidx.drawerlayout.widget.DrawerLayout; // Added import for DrawerLayout
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.widget.Toast;

import com.example.android_app.BuildConfig;
import com.example.android_app.R;
import com.example.android_app.model.Email;
import com.example.android_app.model.viewmodel.InboxViewModel;
import com.example.android_app.ui.EmailAdapter;
import com.example.android_app.ui.EmailDetailsActivity;
import com.example.android_app.ui.fragments.CreateMailFragment;
import com.example.android_app.ui.fragments.SideBarFragment; // Added import for SideBarFragment

import java.util.ArrayList;

public class InboxActivity extends AppCompatActivity implements SideBarFragment.SideBarFragmentListener {

    private InboxViewModel viewModel;
    private EmailAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingProgressBar;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private EditText searchEditText; // Declare EditText for search

    private String profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);

        toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set the navigation click listener for the toolbar to control the drawer
        // ActionBarDrawerToggle manages the icon and its click, but you can override it for custom behavior.
        // If the hamburger icon should be on the right, you will need to set an explicit
        // OnClickListener for a custom ImageView placed in the toolbar, and disable
        // ActionBarDrawerToggle's default indicator.
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (drawerLayout.isDrawerOpen(Gravity.LEFT)) { // Use Gravity.START for RTL compatibility
                    drawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    drawerLayout.openDrawer(Gravity.LEFT);
                }
            }
        });

        // Initialize search bar and profile picture
        searchEditText = findViewById(R.id.search_edit_text);
        ImageView profilePicture = findViewById(R.id.profile_picture);

        // Set up search action listener for the EditText
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(searchEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });

        // Set up profile picture click listener (optional: opens another fragment/activity)
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(InboxActivity.this, "Profile picture clicked!", Toast.LENGTH_SHORT).show();
                // Example: Open a profile fragment or activity
                // getSupportFragmentManager().beginTransaction()
                //         .replace(R.id.fragment_container, new ProfileFragment())
                //         .addToBackStack(null)
                //         .commit();
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        recyclerView = findViewById(R.id.recyclerViewEmails);

        viewModel = new ViewModelProvider(this).get(InboxViewModel.class);

        setupRecyclerView();
        setupRefreshListener();
        observeViewModel();

        // Load the SideBarFragment into the sidebar_container
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sidebar_container, new SideBarFragment())
                    .commit();
        }

        Log.d("MyDebug", "Activity onCreate: Calling viewModel.fetchInbox()");
        viewModel.fetchEmails();

        findViewById(R.id.fabCompose).setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentCreateMailContainer, new CreateMailFragment())
                    .addToBackStack("compose")
                    .commit();

            findViewById(R.id.fragmentCreateMailContainer).setVisibility(View.VISIBLE);
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().findFragmentById(R.id.fragmentCreateMailContainer) == null) {
                viewModel.fetchEmails();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewEmails);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
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
            if (emails != null) {
                adapter.setEmails(emails);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading == null) return;

            if (swipeRefreshLayout.isRefreshing()) {
                if (!isLoading) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            } else {
                loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // This will handle other menu items you might add to the toolbar.
        // The navigation icon's click is handled by toolbar.setNavigationOnClickListener().
        return super.onOptionsItemSelected(item);
    }

    // Implement the SideBarFragmentListener methods
    @Override
    public void onCategorySelected(String categoryName) {
        Log.d("InboxActivity", "Category selected: " + categoryName);
        drawerLayout.closeDrawers(); // Close the drawer(s) after selection
        // Handle category selection (e.g., filter emails)
    }

    @Override
    public void onLabelSelected(String labelId, String labelName) {
        Log.d("InboxActivity", "Label selected: " + labelName + " (ID: " + labelId + ")");
        drawerLayout.closeDrawers(); // Close the drawer(s) after selection
        // Handle label selection (e.g., filter emails)
    }

    /**
     * Placeholder method for performing the search.
     * You would implement your email filtering logic here.
     * @param query The search query entered by the user.
     */
    private void performSearch(String query) {
        Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
        // Implement your search logic here, e.g.,
        // viewModel.searchEmails(query);
    }

    public String getProfileImage() {
        if (profileImage != null && !profileImage.isEmpty() && !profileImage.startsWith("http")) {
            return BuildConfig.SERVER_URL + profileImage;
        }
        return profileImage;
    }
}