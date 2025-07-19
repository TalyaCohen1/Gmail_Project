package com.example.android_app.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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
import com.example.android_app.data.network.ApiClient;
import com.example.android_app.data.network.ApiService;
import com.example.android_app.model.Email;
import com.example.android_app.model.Label;
import com.example.android_app.model.User;
import com.example.android_app.model.viewmodel.InboxViewModel;
import com.example.android_app.model.viewmodel.LabelViewModel;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InboxActivity extends AppCompatActivity implements
        EmailAdapter.EmailItemClickListener,
        EmailAdapter.MultiSelectModeListener,
        SideBarFragment.SideBarFragmentListener,
        EditProfileFragment.OnProfilePictureUpdatedListener {

    private InboxViewModel viewModel;
    private MailViewModel viewModel_mail;
    private LabelViewModel labelViewModel;
    private EmailAdapter adapter;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loadingProgressBar;
    private androidx.lifecycle.Observer<List<Label>> labelsObserver;
    private PopupMenu currentPopupMenu;
    private boolean isPopupMenuReadyToShow = false;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private EditText searchEditText; // Declare EditText for search
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

    private ActivityResultLauncher<Intent> emailDetailsLauncher;

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

        if (userId != null && !userId.isEmpty()) {
            ApiService apiService = ApiClient.getClient().create(ApiService.class);
            Call<User> call = apiService.getUserById(userId);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(Call<User> call, Response<User> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        String email = response.body().getEmailAddress();
                        if (email != null && !email.isEmpty()) {
                            SharedPrefsManager.save(InboxActivity.this, "emailAddress", email);
                            viewModel.fetchEmailsForCategoryOrLabel("inbox");
                        }
                    } else {
                        Log.e("InboxActivity", "Failed to fetch user. Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<User> call, Throwable t) {
                    Log.e("InboxActivity", "Error fetching user from server: " + t.getMessage());
                }
            });
        } else {
            Log.e("InboxActivity", "userId is null or empty");
        }

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
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(profilePicture);
        } else {
            profilePicture.setImageResource(R.drawable.default_profile);
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

        // Set up profile picture click listener
        profilePicture.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(new ContextThemeWrapper(this, R.style.PopupMenuStyle), v);
            popup.getMenuInflater().inflate(R.menu.menu_profile_popup, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_edit_profile) {
                    //go to edit profile fragment
                    // CORRECTED PART: Create fragment instance, set listener, then replace
                    EditProfileFragment editProfileFragment = new EditProfileFragment();
                    editProfileFragment.setOnProfilePictureUpdatedListener(this);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragmentProfileContainer, editProfileFragment)
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
        labelViewModel = new ViewModelProvider(this).get(LabelViewModel.class);

        getSupportFragmentManager().setFragmentResultListener(
                CreateMailFragment.REQUEST_KEY_EMAIL_SENT,
                this,
                (requestKey, result) -> {
                    if (requestKey.equals(CreateMailFragment.REQUEST_KEY_EMAIL_SENT)) {
                        boolean emailSentSuccess = result.getBoolean(CreateMailFragment.BUNDLE_KEY_EMAIL_SENT_SUCCESS, false);
                        if (emailSentSuccess) {
                            String currentCategory = viewModel.getCurrentCategoryOrLabelId();
                            if (currentCategory == null || currentCategory.isEmpty()) {
                                viewModel.fetchEmailsForCategoryOrLabel("inbox");
                            } else {
                                viewModel.fetchEmailsForCategoryOrLabel(currentCategory);
                            }
                            findViewById(R.id.fragmentCreateMailContainer).setVisibility(View.GONE);
                            findViewById(R.id.fabCompose).setVisibility(View.VISIBLE);
                        }
                    }
                }
        );

        // Load the SideBarFragment into the sidebar_container
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.sidebar_container, new SideBarFragment())
                    .commit();
        }

        viewModel.fetchEmailsForCategoryOrLabel("inbox");

        findViewById(R.id.fabCompose).setOnClickListener(v -> {
            if (adapter.isMultiSelectMode()) {
                adapter.clearSelection();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentCreateMailContainer, new CreateMailFragment())
                    .addToBackStack("compose")
                    .commit();

            findViewById(R.id.fragmentCreateMailContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.fabCompose).setVisibility(View.GONE);
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            // Refresh the inbox when the CreateMailFragment is popped from back stack
            if (getSupportFragmentManager().findFragmentById(R.id.fragmentCreateMailContainer) == null) {
                viewModel.fetchEmailsForCategoryOrLabel(viewModel.getCurrentCategoryOrLabelId());
                findViewById(R.id.fragmentCreateMailContainer).setVisibility(View.GONE);
                findViewById(R.id.fabCompose).setVisibility(View.VISIBLE);
            }
        });

        String currentUserEmail = SharedPrefsManager.get(this, "emailAddress");
        String token = SharedPrefsManager.get(this, "token");

        if (currentUserEmail == null || currentUserEmail.isEmpty() || token == null || token.isEmpty()) {

        }

        //back from sending email
        emailDetailsLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();

                        if (data != null && data.getBooleanExtra("email_sent_from_details", false)) {
                            String currentCategory = viewModel.getCurrentCategoryOrLabelId();
                            if (currentCategory == null || currentCategory.isEmpty()) {
                                viewModel.fetchEmailsForCategoryOrLabel("inbox");
                            } else {
                                viewModel.fetchEmailsForCategoryOrLabel(currentCategory);
                            }
                        }
                    }
                }
        );

        setupRecyclerView();
        setupMultiSelectToolbarListeners();
        setupRefreshListener();
        observeViewModel();
    }

    private void loadProfileImage() {
        // Retrieve current user data from UserManager (which should be updated after profile save)
        String userId = UserManager.getUserId(this);
        String fullName = UserManager.getFullName(this);
        String profileImageUrl = UserManager.getProfileImage(this); // This should be the latest URL

        // Update the User object if needed, or simply use the fetched values
         currentUser = new User(userId, fullName, profileImageUrl); // Only if you actively use currentUser object later

        if (profilePicture != null) {
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                String fullUrl;
                if (!profileImageUrl.startsWith("http") && !profileImageUrl.startsWith("content://") && !profileImageUrl.startsWith("file://")) {
                    fullUrl = BuildConfig.SERVER_URL + profileImageUrl;
                } else {
                    fullUrl = profileImageUrl;
                }
                Glide.with(this)
                        .load(fullUrl)
                        .placeholder(R.drawable.default_profile) // Your default placeholder
                        .error(R.drawable.default_profile)     // Your error image
                        .circleCrop() // If you want circular images
                        .into(profilePicture);
            } else {
                profilePicture.setImageResource(R.drawable.default_profile);
            }
        }
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
            viewModel.fetchEmailsForCategoryOrLabel(viewModel.getCurrentCategoryOrLabelId());
        });
    }

    private void setupMultiSelectToolbarListeners() {
        labelViewModel.fetchLabels();
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
                            Toast.makeText(this, "Deleting selected mails", Toast.LENGTH_SHORT).show();
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
            }
        });
        iconMoreOptions.setOnClickListener(this::showMoreOptionsPopupMenu);
    }
    private void showMoreOptionsPopupMenu(View anchorView) {
        if (currentPopupMenu != null) {
            currentPopupMenu.dismiss();
        }

        currentPopupMenu = new PopupMenu(this, anchorView);
        currentPopupMenu.getMenuInflater().inflate(R.menu.menu_multi_select_more, currentPopupMenu.getMenu());

        isPopupMenuReadyToShow = false;

        if (labelsObserver != null) {
            labelViewModel.getLabels().removeObserver(labelsObserver);
        }

        labelsObserver = new androidx.lifecycle.Observer<List<Label>>() {
            @Override
            public void onChanged(List<Label> labels) {
                Menu labelsSubMenu = currentPopupMenu.getMenu().findItem(R.id.action_add_to_label).getSubMenu();
                if (labelsSubMenu != null) {
                    labelsSubMenu.clear();
                    if (labels != null && !labels.isEmpty()) {
                        for (Label label : labels) {
                            labelsSubMenu.add(Menu.NONE,
                                            View.generateViewId(),
                                            Menu.NONE,
                                            label.getName()) // Title
                                    .setOnMenuItemClickListener(item -> {
                                        List<Email> selectedEmails = adapter.getSelectedEmails();
                                        if (selectedEmails.isEmpty()) {
                                            Toast.makeText(InboxActivity.this, "No emails selected.", Toast.LENGTH_SHORT).show();
                                            return true;
                                        }
                                        for (Email email : selectedEmails) {
                                            labelViewModel.addMailToLabel(label.getId(), email.getId());
                                        }
                                        Toast.makeText(InboxActivity.this, "Adding to label: " + label.getName() + "...", Toast.LENGTH_SHORT).show();
                                        adapter.clearSelection();
                                        return true;
                                    });
                        }
                    } else {
                        labelsSubMenu.add(Menu.NONE, Menu.NONE, Menu.NONE, "No labels available");
                    }
                }

                if (!isPopupMenuReadyToShow) {
                    isPopupMenuReadyToShow = true;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (currentPopupMenu != null) {
                            currentPopupMenu.show();
                        }
                    });
                }
            }
        };

        labelViewModel.getLabels().observe(this, labelsObserver);
        labelViewModel.fetchLabels();

        currentPopupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            List<Email> selectedEmails = adapter.getSelectedEmails();
            if (selectedEmails.isEmpty()) {
                Toast.makeText(this, "No emails selected.", Toast.LENGTH_SHORT).show();
                return true;
            }

            if (id == R.id.action_mark_important) {
                for (Email email : selectedEmails) {
                    viewModel.markEmailAsImportant(email.getId());
                }
                Toast.makeText(this, "Marking as important", Toast.LENGTH_SHORT).show();
                adapter.clearSelection();
                return true;
            }
            return false;
        });

        currentPopupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                if (labelsObserver != null) {
                    labelViewModel.getLabels().removeObserver(labelsObserver);
                    labelsObserver = null;
                }
                currentPopupMenu = null;
                isPopupMenuReadyToShow = false;
            }
        });
    }

    @Override
    public void onMultiSelectModeChanged(boolean inMultiSelectMode) {

        // Get both toolbar references
        Toolbar toolbar = findViewById(R.id.toolbar);
        ConstraintLayout multiSelectToolbar = findViewById(R.id.multiSelectToolbar);

        // Toggle visibility with logging
        toolbar.setVisibility(inMultiSelectMode ? View.GONE : View.VISIBLE);
        multiSelectToolbar.setVisibility(inMultiSelectMode ? View.VISIBLE : View.GONE);

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
//            startActivity(intent);
            emailDetailsLauncher.launch(intent);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, which will handle the drawer open/close.
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Implement the SideBarFragmentListener methods
    @Override
    public void onCategorySelected(String categoryName) {
        drawerLayout.closeDrawers(); // Close the drawer after selection
        viewModel.fetchEmailsForCategoryOrLabel(categoryName.toLowerCase(Locale.ROOT));
    }

    @Override
    public void onLabelSelected(String labelId, String labelName) {
        drawerLayout.closeDrawers(); // Close the drawer after selection
        viewModel.fetchEmailsForCategoryOrLabel(labelId);
    }

    /**
     * Placeholder method for performing the search.
     * You would implement your email filtering logic here.
     * @param query The search query entered by the user.
     */
    private void performSearch(String query) {
        viewModel_mail.searchMails(query);
        viewModel_mail.getSearchResults().observe(this, emails -> {
            if (emails != null) {
                adapter.setEmails(emails);
            }
        });
    }

    private void performLogout() {
        //clear shared prefs
        SharedPreferences prefs = getSharedPreferences("SmailPrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();

        //back to Login Screen
        Intent intent = new Intent(InboxActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }

    @Override
    public void onProfilePictureUpdated() {
        // This method will be called by EditProfileFragment when done
        loadProfileImage(); //call to relode image
    }
}