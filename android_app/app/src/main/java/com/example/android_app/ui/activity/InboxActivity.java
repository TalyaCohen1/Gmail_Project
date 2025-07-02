package com.example.android_app.ui.activity; // Make sure this package is correct

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.android_app.BuildConfig;
import com.example.android_app.R;
import com.example.android_app.model.Email;
import com.example.android_app.model.Label;
import com.example.android_app.model.User;
import com.example.android_app.model.viewmodel.EditProfileViewModel;
import com.example.android_app.model.viewmodel.InboxViewModel;
import com.example.android_app.model.viewmodel.MailViewModel;
import com.example.android_app.ui.EmailAdapter;
import com.example.android_app.ui.EmailDetailsActivity;
import com.example.android_app.ui.fragments.CreateMailFragment;
import com.example.android_app.ui.fragments.EditProfileFragment;
import com.example.android_app.ui.fragments.SideBarFragment;
import com.example.android_app.utils.SharedPrefsManager;
import com.example.android_app.utils.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class InboxActivity extends AppCompatActivity implements
        EmailAdapter.EmailItemClickListener, // Implement the new interface
        EmailAdapter.MultiSelectModeListener,
        SideBarFragment.SideBarFragmentListener { // Implement the new interface

    private InboxViewModel viewModel;

    private MailViewModel viewModel_mail;
    private EmailAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingProgressBar;

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private EditText searchEditText; // Declare EditText for search

    private String profileImage;

    private ImageView profilePicture; // Declare ImageView for profile picture
    private User currentUser; // Declare a User object

    // Multi-select UI elements
    private ConstraintLayout multiSelectToolbar;
    private Toolbar toolbar;
    private TextView selectedCountTextView;
    private ImageView iconCloseMultiSelect;
    private ImageView iconDelete;
    private ImageView iconMarkReadUnread;
    private ImageView iconMoreOptions;
    private ImageView iconSpam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        toolbar = findViewById(R.id.toolbar);
        multiSelectToolbar = findViewById(R.id.multiSelectToolbar);
        setSupportActionBar(toolbar);


        profilePicture = findViewById(R.id.profile_picture); // Initialize the ImageView

        // Retrieve user data from SharedPrefsManager
        String userId = SharedPrefsManager.get(this, "userId");
        String fullName = SharedPrefsManager.get(this, "fullName");
        String profileImage = SharedPrefsManager.get(this, "profileImage");

        // Construct the full URL for the profile image if it's a relative path
        if (profileImage != null && !profileImage.isEmpty() && !profileImage.startsWith("http")) {
            profileImage = BuildConfig.SERVER_URL + profileImage;
        }

        // Create the User object
        currentUser = new User(userId, fullName, profileImage);

        // Load the profile picture using Glide
        if (currentUser != null && currentUser.getProfilePicUrl() != null && !currentUser.getProfilePicUrl().isEmpty()) {
            Glide.with(this)
                    .load(currentUser.getProfilePicUrl())
                    .placeholder(R.drawable.default_profile) // Optional: default image while loading
                    .error(R.drawable.default_profile) // Optional: image to show if loading fails
                    .circleCrop() // Optional: to make the image circular
                    .into(profilePicture);
            Log.d("InboxActivity", "Profile picture loaded: " + currentUser.getProfilePicUrl());
        } else {
            profilePicture.setImageResource(R.drawable.default_profile);
            Log.d("InboxActivity", "User profile URL is missing or null, showing default profile picture.");
        }

        // Set up profile picture click listener (optional)
        profilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(InboxActivity.this, "Profile picture clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        // Hide the default title as we have a custom layout within the toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

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

        // Initialize search bar and profile picture
        searchEditText = findViewById(R.id.search_edit_text);
        ImageView profilePicture = findViewById(R.id.profile_picture);

        EditProfileViewModel editProfileViewModel = new ViewModelProvider(this).get(EditProfileViewModel.class);
        String profileImageUrl = UserManager.getProfileImage(this); // UserManager.getProfileImage already provides a default of null
        // Then handle the default image logic if profileImageUrl is null
        if (profileImageUrl == null || profileImageUrl.isEmpty()) {
            profileImageUrl = "/uploads/default-profile.png"; // Or handle this within UserManager.getProfileImage
        }
        String fullUrl;
        if (!profileImageUrl.startsWith("http")) {
            fullUrl = BuildConfig.SERVER_URL + profileImageUrl;
        } else {
            fullUrl = profileImageUrl;
        }

        // put it into the ImageView
        Glide.with(this)
                .load(fullUrl)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .circleCrop()
                .into(profilePicture);


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

        // Set up profile picture click listener
        profilePicture.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(InboxActivity.this, v);
            popup.getMenuInflater().inflate(R.menu.menu_profile_popup, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit_profile) {
                    //go to edit profile fragment
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentProfileContainer, new EditProfileFragment())
                            .addToBackStack("editProfile")
                            .commit();
                    findViewById(R.id.fragmentProfileContainer).setVisibility(View.VISIBLE);
                    return true;

                } else if (id == R.id.action_logout) {
                    //logout from this user
                    performLogout();
                    return true;
                }
                return false;
            });

            popup.show();
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().findFragmentById(R.id.fragmentProfileContainer) == null) {
                findViewById(R.id.fragmentProfileContainer).setVisibility(View.GONE);
            }
        });



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
        iconSpam = findViewById(R.id.iconSpam);


        viewModel = new ViewModelProvider(this).get(InboxViewModel.class);
        viewModel_mail = new ViewModelProvider(this).get(MailViewModel.class);

        getSupportFragmentManager().setFragmentResultListener(
                CreateMailFragment.REQUEST_KEY_EMAIL_SENT, // המפתח שהגדרת ב-CreateMailFragment
                this, // ה-LifecycleOwner הוא ה-Activity עצמו
                (requestKey, result) -> {
                    Log.d("InboxActivity", "Received fragment result. Request Key: " + requestKey);
                    // Callback כאשר מתקבלת תוצאה
                    if (requestKey.equals(CreateMailFragment.REQUEST_KEY_EMAIL_SENT)) {
                        // בדוק אם המייל נשלח בהצלחה על פי הנתונים בחבילה (Bundle)
                        boolean emailSentSuccess = result.getBoolean(CreateMailFragment.BUNDLE_KEY_EMAIL_SENT_SUCCESS, false);
                        Log.d("InboxActivity", "Email sent success flag: " + emailSentSuccess);
                        if (emailSentSuccess) {
                            // המייל נשלח בהצלחה. כעת רענן את האינבוקס.
                            String currentCategory = viewModel.getCurrentCategoryOrLabelId();
                            Log.d("InboxActivity", "Email sent successfully. Refreshing category: " + currentCategory);
                            if (currentCategory == null || currentCategory.isEmpty()) {
                                // רענן את האינבוקס הראשי אם אין קטגוריה ספציפית פעילה
                                viewModel.fetchEmailsForCategoryOrLabel("inbox");
                            } else {
                                // רענן את הקטגוריה הפעילה הנוכחית (לדוגמה, "inbox", "sent", "allmail")
                                viewModel.fetchEmailsForCategoryOrLabel(currentCategory);
                            }
                            Toast.makeText(this, "המייל נשלח בהצלחה!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        setupRecyclerView();
        setupMultiSelectToolbarListeners(); // New method for toolbar listeners
        setupRefreshListener();
        observeViewModel();


        // Load the SideBarFragment into the sidebar_container
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sidebar_container, new SideBarFragment())
                    .commit();
        }

        Log.d("MyDebug", "Activity onCreate: Calling viewModel.fetchInbox()");
        viewModel.fetchEmailsForCategoryOrLabel("inbox");

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
                viewModel.fetchEmailsForCategoryOrLabel(viewModel.getCurrentCategoryOrLabelId());
            }
        });
    }

//    @Override
//    public void onToggleEmailStarred(Email email, int position) {
//        // --- THIS IS THE MISSING METHOD YOU NEED TO ADD ---
//
//        // 1. Toggle the 'starred' status in your data model
//        // Assuming your Email object is mutable and email.setStarred() works
//        email.setStarred(!email.isStarred());
//
//        // 2. Notify the adapter that this specific item's data has changed.
//        // This will cause onBindViewHolder to be re-executed for this item,
//        // which will update the star icon based on the new email.isStarred() state.
//        adapter.notifyItemChanged(position);
//
//        // 3. (Optional) Show a Toast message or update UI
//        String message = email.isStarred() ? "Marked as starred" : "Unmarked as starred";
//        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//
//        // 4. (Important) If you're using a ViewModel or persistent storage (like a database),
//        // you would typically call a method on your ViewModel here to persist this change.
//        // For example:
//        // myEmailViewModel.updateEmailStarredStatus(email.getId(), email.isStarred());
//    }


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
            viewModel.fetchEmailsForCategoryOrLabel(viewModel.getCurrentCategoryOrLabelId());
        });
    }

    private void setupMultiSelectToolbarListeners() {
        iconCloseMultiSelect.setOnClickListener(v -> {
            adapter.clearSelection(); // Exit multi-select mode
        });
        iconSpam.setOnClickListener(v -> {
            List<Email> selectedEmails = adapter.getSelectedEmails();
            if (selectedEmails.isEmpty()) {
                Toast.makeText(this, "No emails selected.", Toast.LENGTH_SHORT).show();
                return;
            }
            
            for (Email email : selectedEmails) {
                viewModel.markEmailAsSpam(email.getId());
            }
            Toast.makeText(this, "Marked as spam", Toast.LENGTH_SHORT).show();
            adapter.clearSelection();
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
                                        viewModel.addMailToLabel(email.getId(), label.getId());
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
            viewModel.getLabels().removeObservers(this); // Remove observer after use
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
//            } else if (id == R.id.action_mark_spam) {
//                for (Email email : selectedEmails) {
//                    viewModel.markEmailAsSpam(email.getId());
//                }
//                Toast.makeText(this, "Reporting as spam...", Toast.LENGTH_SHORT).show();
//                adapter.clearSelection();
//                return true;
//            } else if (id == R.id.action_move_to_folder) {
//                // This would typically open another dialog or activity to select a folder
//                Toast.makeText(this, "Move to folder functionality (not implemented yet)", Toast.LENGTH_SHORT).show();
//                return true;
            }
            // For labels, the listener is set directly on each submenu item, so it won't reach here.
            return false;
        });
        popup.show();
    }

    // --- Callbacks from EmailAdapter.MultiSelectModeListener ---
    // @Override
    // public void onMultiSelectModeChanged(boolean inMultiSelectMode) {
    //     multiSelectToolbar.setVisibility(inMultiSelectMode ? View.VISIBLE : View.GONE);
    //     findViewById(R.id.fabCompose).setVisibility(inMultiSelectMode ? View.GONE : View.VISIBLE); // Hide FAB

    //     // You might want to change the status bar color here as well
    //     // getWindow().setStatusBarColor(ContextCompat.getColor(this,
    //     //         inMultiSelectMode ? R.color.selected_toolbar_color : R.color.colorPrimaryDark));
    // }
//    @Override
//    public void onMultiSelectModeChanged(boolean inMultiSelectMode) {
//        Log.d("DEBUG_MULTISELECT", "InboxActivity onMultiSelectModeChanged received: " + inMultiSelectMode); // Add this
//
//        multiSelectToolbar.setVisibility(inMultiSelectMode ? View.VISIBLE : View.GONE);
//        Log.d("DEBUG_MULTISELECT", "multiSelectToolbar visibility set to: " + (inMultiSelectMode ? "VISIBLE" : "GONE")); // Add this
//
//        // Crucial change: Toggle visibility of the regular toolbar
//        toolbar.setVisibility(inMultiSelectMode ? View.GONE : View.VISIBLE); // ADD THIS LINE
//        Log.d("DEBUG_MULTISELECT", "Regular toolbar visibility set to: " + (inMultiSelectMode ? "GONE" : "VISIBLE")); // Add this
//
//        findViewById(R.id.fabCompose).setVisibility(inMultiSelectMode ? View.GONE : View.VISIBLE); // Hide FAB
//
//        // You might want to change the status bar color here as well
//        // getWindow().setStatusBarColor(ContextCompat.getColor(this,
//        //         inMultiSelectMode ? R.color.selected_toolbar_color : R.color.colorPrimaryDark));
//    }
    @Override
    public void onMultiSelectModeChanged(boolean inMultiSelectMode) {
        Log.d("MultiSelect", "Mode changed to: " + (inMultiSelectMode ? "active" : "inactive"));
        
        // Get both toolbar references
        Toolbar toolbar = findViewById(R.id.toolbar);
        ConstraintLayout multiSelectToolbar = findViewById(R.id.multiSelectToolbar);
        if (multiSelectToolbar.getChildCount() == 0) {
            Log.e("MultiSelect", "MultiSelectToolbar has no child views!");
        }
        
        // Toggle visibility with logging
        toolbar.setVisibility(inMultiSelectMode ? View.GONE : View.VISIBLE);
        multiSelectToolbar.setVisibility(inMultiSelectMode ? View.VISIBLE : View.GONE);
        
        Log.d("MultiSelect", "Toolbar visibility: " + 
            (toolbar.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
        Log.d("MultiSelect", "MultiSelectToolbar visibility: " + 
            (multiSelectToolbar.getVisibility() == View.VISIBLE ? "VISIBLE" : "GONE"));
        
        findViewById(R.id.fabCompose).setVisibility(inMultiSelectMode ? View.GONE : View.VISIBLE);
        multiSelectToolbar.requestLayout();
        multiSelectToolbar.invalidate();
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
    public void onMarkAsStarred(String mailId) {
        viewModel.markEmailAsStarred(mailId);
        Toast.makeText(this, "Marked as starred", Toast.LENGTH_SHORT).show();
    }
    public void onUnmarkAsStarred(String mailId) {
        viewModel.unmarkEmailAsStarred(mailId);
        Toast.makeText(this, "Unmarked as starred", Toast.LENGTH_SHORT).show();
    }

    public void onUnmarkAsImportant(String mailId) {
        viewModel.unmarkEmailAsImportant(mailId);
        Toast.makeText(this, "Unmarked as starred", Toast.LENGTH_SHORT).show();
    }

    public void onMarkAsImportant(String mailId) {
        viewModel.markEmailAsImportant(mailId);
        Toast.makeText(this, "Marked as starred", Toast.LENGTH_SHORT).show();
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
        viewModel.getCurrentEmails().observe(this, emails -> {
            if (emails != null) {
                // List<Email> emailsToShow = MailMapper.toEmails(emails);
                // adapter.setEmails(emailsToShow);
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

//    @Override
//    public void onBackPressed() {
//        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
//            drawerLayout.closeDrawer(Gravity.LEFT);
//        } else {
//            super.onBackPressed();
//        }
//    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, which will handle the drawer open/close.
        if (toggle.onOptionsItemSelected(item)) { // ADD THIS BLOCK
            return true;
        }
        return super.onOptionsItemSelected(item); // Keep this for other menu items
    }

    // Implement the SideBarFragmentListener methods
    @Override
    public void onCategorySelected(String categoryName) {
        Log.d("InboxActivity", "onCategorySelected received: " + categoryName);
        drawerLayout.closeDrawers(); // Close the drawer(s) after selection
        // Handle category selection (e.g., filter emails)
        viewModel.fetchEmailsForCategoryOrLabel(categoryName.toLowerCase(Locale.ROOT));
    }

    @Override
    public void onLabelSelected(String labelId, String labelName) {
        Log.d("InboxActivity", "Label selected: " + labelName + " (ID: " + labelId + ")");
        drawerLayout.closeDrawers(); // Close the drawer(s) after selection
        // Handle label selection (e.g., filter emails)
        viewModel.fetchEmailsForCategoryOrLabel(labelId);
    }

    // In InboxActivity.java, add this method
    private void updateToolbarForSearchState() {
        // Determine if search is active based on the search EditText content
        boolean isSearchActive = !searchEditText.getText().toString().isEmpty();

        if (isSearchActive) {
            // When search is active, show the back arrow
            toggle.setDrawerIndicatorEnabled(false); // Hide the hamburger icon
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true); // Show the Up/Home button (which will be a back arrow)
        } else {
            // When no search is active, show the hamburger icon
            toggle.setDrawerIndicatorEnabled(true); // Show the hamburger icon
            Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(false); // Hide the Up/Home button
        }
        // Synchronize the state of the ActionBarDrawerToggle with the toolbar to reflect changes
        toggle.syncState();
    }

    /**
     * Placeholder method for performing the search.
     * You would implement your email filtering logic here.
     * @param query The search query entered by the user.
     */
    private void performSearch(String query) {
        Toast.makeText(this, "Searching for: " + query, Toast.LENGTH_SHORT).show();
        viewModel_mail.searchMails(query);
        viewModel_mail.getSearchResults().observe(this, emails -> {
            if (emails != null) {
                adapter.setEmails(emails);
                Toast.makeText(this, "Search results: " + emails.size(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public String getProfileImage() {
        if (profileImage != null && !profileImage.isEmpty() && !profileImage.startsWith("http")) {
            return BuildConfig.SERVER_URL + profileImage;
        }
        return profileImage;
    }

    private void performLogout() {
        //clear shared prefs
        SharedPreferences prefs = getSharedPreferences("SmailPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        //back to Login Screen
        Intent intent = new Intent(InboxActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // מנקה את ה־BackStack
        startActivity(intent);

        finish();
    }
}