package com.example.android_app.ui.activity; // Make sure this package is correct

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.android_app.R;
import com.example.android_app.model.Email;
import com.example.android_app.model.Label;
import com.example.android_app.model.viewmodel.InboxViewModel;
import com.example.android_app.ui.EmailAdapter;
import com.example.android_app.ui.EmailDetailsActivity;
import com.example.android_app.ui.fragments.CreateMailFragment;

import java.util.ArrayList;
import java.util.List;

public class InboxActivity extends AppCompatActivity implements
        EmailAdapter.EmailItemClickListener, // Implement the new interface
        EmailAdapter.MultiSelectModeListener { // Implement the new interface

    private InboxViewModel viewModel;
    private EmailAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingProgressBar;

    // Multi-select UI elements
    private ConstraintLayout multiSelectToolbar;
    private TextView selectedCountTextView;
    private ImageView iconCloseMultiSelect;
    private ImageView iconDelete;
    private ImageView iconMarkReadUnread;
    private ImageView iconMoreOptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox); // Ensure activity_inbox.xml is updated

        // Initialize views
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        recyclerView = findViewById(R.id.recyclerViewEmails);

        // Initialize multi-select UI elements
        multiSelectToolbar = findViewById(R.id.multiSelectToolbar);
        selectedCountTextView = findViewById(R.id.selectedCountTextView);
        iconCloseMultiSelect = findViewById(R.id.iconCloseMultiSelect);
        iconDelete = findViewById(R.id.iconDelete);
        iconMarkReadUnread = findViewById(R.id.iconMarkReadUnread);
        iconMoreOptions = findViewById(R.id.iconMoreOptions);


        viewModel = new ViewModelProvider(this).get(InboxViewModel.class);

        setupRecyclerView();
        setupMultiSelectToolbarListeners(); // New method for toolbar listeners
        setupRefreshListener();
        observeViewModel();

        Log.d("MyDebug", "Activity onCreate: Calling viewModel.fetchEmails()");
        viewModel.fetchEmails(); // Fetch initial emails

        findViewById(R.id.fabCompose).setOnClickListener(v -> {
            // If in multi-select mode, exit it when compose button is clicked (optional, but good UX)
            if (adapter.isMultiSelectMode()) {
                adapter.clearSelection();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentCreateMailContainer, new CreateMailFragment())
                    .addToBackStack("compose")
                    .commit();

            findViewById(R.id.fragmentCreateMailContainer).setVisibility(View.VISIBLE);
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            // Refresh the inbox when the CreateMailFragment is popped from back stack
            if (getSupportFragmentManager().findFragmentById(R.id.fragmentCreateMailContainer) == null) {
                viewModel.fetchEmails();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EmailAdapter(this, new ArrayList<>(), this); // Pass 'this' as EmailItemClickListener
        adapter.setMultiSelectModeListener(this); // Set this activity as MultiSelectModeListener
        recyclerView.setAdapter(adapter);
    }

    private void setupRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (adapter.isMultiSelectMode()) { // Prevent refresh while in multi-select
                swipeRefreshLayout.setRefreshing(false);
                return;
            }
            viewModel.fetchEmails();
        });
    }

    private void setupMultiSelectToolbarListeners() {
        iconCloseMultiSelect.setOnClickListener(v -> {
            adapter.clearSelection(); // Exit multi-select mode
        });

        iconDelete.setOnClickListener(v -> {
            List<Email> selectedEmails = adapter.getSelectedEmails();
            if (!selectedEmails.isEmpty()) {
                new AlertDialog.Builder(this)
                        .setTitle("Delete Emails")
                        .setMessage("Are you sure you want to delete " + selectedEmails.size() + " selected emails?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // Call ViewModel to delete each selected email
                            for (Email email : selectedEmails) {
                                viewModel.deleteEmail(email.getId());
                            }
                            adapter.clearSelection(); // Clear selection after action
                            Toast.makeText(this, "Deleting selected emails...", Toast.LENGTH_SHORT).show();
                            // Fetch emails to update UI after deletion, could be done in ViewModel callback too
                            // viewModel.fetchEmails(); // This will be called via observeViewModel anyway
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

        iconMarkReadUnread.setOnClickListener(v -> {
            List<Email> selectedEmails = adapter.getSelectedEmails();
            if (!selectedEmails.isEmpty()) {
                // Determine if all selected are read or unread to decide the action
                boolean allRead = true;
                for (Email email : selectedEmails) {
                    if (!email.isRead()) { // Assuming `isRead()` exists on your Email model
                        allRead = false;
                        break;
                    }
                }

                if (allRead) { // If all are read, mark them as unread
                    for (Email email : selectedEmails) {
                        viewModel.markEmailAsUnread(email.getId());
                    }
                    Toast.makeText(this, "Marked as unread", Toast.LENGTH_SHORT).show();
                } else { // If any are unread, mark all as read
                    for (Email email : selectedEmails) {
                        viewModel.markEmailAsRead(email.getId());
                    }
                    Toast.makeText(this, "Marked as read", Toast.LENGTH_SHORT).show();
                }
                adapter.clearSelection(); // Clear selection after action
                // viewModel.fetchEmails(); // This will be called via observeViewModel anyway
            }
        });

        iconMoreOptions.setOnClickListener(this::showMoreOptionsPopupMenu);
    }

    private void showMoreOptionsPopupMenu(View anchorView) {
        PopupMenu popup = new PopupMenu(this, anchorView);
        // *** שינוי כאן: ניפוח הקובץ החדש menu_multi_select_more.xml ***
        popup.getMenuInflater().inflate(R.menu.menu_multi_select_more, popup.getMenu());

        // Fetch labels to populate the "Add to Label" submenu dynamically
        // Observe labels once when the popup is shown to populate the submenu
        viewModel.getLabels().observe(this, labels -> {
            // Find the "Add to Label" item in the popup menu
            Menu labelsSubMenu = popup.getMenu().findItem(R.id.action_add_to_label).getSubMenu();
            if (labelsSubMenu != null) {
                labelsSubMenu.clear(); // Clear existing items before adding new ones
                if (labels != null && !labels.isEmpty()) {
                    for (Label label : labels) { // Assuming `Label` model has getId() and getName()
                        labelsSubMenu.add(Menu.NONE, // Group ID (none)
                                        View.generateViewId(), // Item ID (unique, can be Menu.NONE if not needed for direct lookup)
                                        Menu.NONE, // Order (none)
                                        label.getName()) // Title
                                .setOnMenuItemClickListener(item -> {
                                    List<Email> selectedEmails = adapter.getSelectedEmails();
                                    if (selectedEmails.isEmpty()) {
                                        Toast.makeText(this, "No emails selected.", Toast.LENGTH_SHORT).show();
                                        return true;
                                    }
                                    for (Email email : selectedEmails) {
                                        viewModel.addLabelToEmail(email.getId(), label.getId()); // Call ViewModel to add label
                                    }
                                    Toast.makeText(this, "Adding to label: " + label.getName() + "...", Toast.LENGTH_SHORT).show();
                                    adapter.clearSelection();
                                    // viewModel.fetchEmails(); // Refresh emails (optional, ViewModel callbacks might do it)
                                    return true;
                                });
                    }
                } else {
                    labelsSubMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, "No labels available");
                }
            }
            // Ensure the observer is removed to prevent multiple additions if popup is opened multiple times
            // This is a bit tricky with LiveData and PopupMenu, consider using `removeObservers` if needed
            // For now, `labelsSubMenu.clear()` handles re-adding, but `removeObservers` is cleaner for single-shot updates.
            // Or, make sure `getLabels()` returns a LiveData that doesn't re-trigger on config changes.
        });

        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId(); // זה ה-ID של פריט התפריט שנבחר מה-menu_multi_select_more.xml
            List<Email> selectedEmails = adapter.getSelectedEmails();
            if (selectedEmails.isEmpty()) {
                Toast.makeText(this, "No emails selected.", Toast.LENGTH_SHORT).show();
                return true;
            }

            // *** שינוי כאן: בדיקה מול ה-IDs החדשים מה-menu_multi_select_more.xml ***
            if (id == R.id.action_mark_important) {
                for (Email email : selectedEmails) {
                    viewModel.markEmailAsImportant(email.getId());
                }
                Toast.makeText(this, "Marking as important...", Toast.LENGTH_SHORT).show();
                adapter.clearSelection();
                return true;
            } else if (id == R.id.action_mark_spam) {
                for (Email email : selectedEmails) {
                    viewModel.markEmailAsSpam(email.getId());
                }
                Toast.makeText(this, "Reporting as spam...", Toast.LENGTH_SHORT).show();
                adapter.clearSelection();
                return true;
            } else if (id == R.id.action_move_to_folder) {
                // This would typically open another dialog or activity to select a folder
                Toast.makeText(this, "Move to folder functionality (not implemented yet)", Toast.LENGTH_SHORT).show();
                return true;
            }
            // For labels, the listener is set directly on each submenu item, so it won't reach here.
            return false;
        });
        popup.show();
    }

    // --- Callbacks from EmailAdapter.MultiSelectModeListener ---
    @Override
    public void onMultiSelectModeChanged(boolean inMultiSelectMode) {
        multiSelectToolbar.setVisibility(inMultiSelectMode ? View.VISIBLE : View.GONE);
        findViewById(R.id.fabCompose).setVisibility(inMultiSelectMode ? View.GONE : View.VISIBLE); // Hide FAB

        // You might want to change the status bar color here as well
        // getWindow().setStatusBarColor(ContextCompat.getColor(this,
        //         inMultiSelectMode ? R.color.selected_toolbar_color : R.color.colorPrimaryDark));
    }

    @Override
    public void onSelectedCountChanged(int count) {
        selectedCountTextView.setText(String.valueOf(count));
        // Dynamically change the "Mark as Read/Unread" icon based on current selection
        if (count > 0) {
            boolean allSelectedAreRead = true;
            for (Email email : adapter.getSelectedEmails()) {
                if (!email.isRead()) { // Assuming `isRead()` exists on your Email model
                    allSelectedAreRead = false;
                    break;
                }
            }
            if (allSelectedAreRead) {
                iconMarkReadUnread.setImageResource(R.drawable.mark_as_unread); // Icon for unread
            } else {
                iconMarkReadUnread.setImageResource(R.drawable.mark_as_read); // Icon for read
            }
        }
    }


    // --- Callbacks from EmailAdapter.EmailItemClickListener ---
    @Override
    public void onEmailClick(Email email) {
        // If not in multi-select mode, navigate to email details
        if (!adapter.isMultiSelectMode()) {
            Intent intent = new Intent(this, EmailDetailsActivity.class);
            intent.putExtra("email_id", email.getId());
            startActivity(intent);
        }
        // If in multi-select mode, the adapter already handled the toggle
    }

    @Override
    public void onEmailLongClick(Email email) {
        // The adapter already handles entering multi-select mode and selecting the item
        // No additional action needed here from the activity's perspective for long click start
    }

    @Override
    public void onBackPressed() {
        if (adapter.isMultiSelectMode()) {
            adapter.clearSelection(); // Exit multi-select mode on back press
        } else {
            super.onBackPressed(); // Perform default back action
        }
    }

    private void observeViewModel() {
        viewModel.getInboxEmails().observe(this, emails -> {
            if (emails != null) {
                adapter.setEmails(emails);
            }
            swipeRefreshLayout.setRefreshing(false); // Stop refresh animation regardless
            loadingProgressBar.setVisibility(View.GONE); // Hide progress bar regardless
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

        // Observe labels for the "Add to label" menu
        viewModel.getLabels().observe(this, labels -> {
            // This observer is mainly to trigger updates for the popup menu
            // The popup menu itself will fetch and use this LiveData.
            // No direct UI update needed here, just ensures data is loaded.
        });
    }
}