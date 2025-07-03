package com.example.android_app.ui.fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.android_app.R;
import com.example.android_app.model.Label;
import com.example.android_app.model.viewmodel.LabelViewModel;
import com.example.android_app.model.viewmodel.MailViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList; // Added for filtering

// Implement LabelDialogFragment.LabelModalListener here
public class SideBarFragment extends Fragment implements LabelDialogFragment.LabelModalListener { //

    private MailViewModel mailViewModel;
    private LabelViewModel labelViewModel;

    // Predefined category label names (must match backend)
    private static final List<String> CATEGORY_LABEL_NAMES = Arrays.asList("Social", "Updates", "Forums", "Promotions");

    // Default categories layouts
    private LinearLayout inboxLayout;
    private LinearLayout starredLayout;
    private LinearLayout sentLayout;
    private LinearLayout draftsLayout;

    // "More" section elements
    private Button moreButton;
    private LinearLayout moreLabelsSection;
    private LinearLayout importantLayout;
    private LinearLayout allMailLayout;
    private LinearLayout spamLayout;
    private LinearLayout trashLayout;

    // Categories sub-section elements (within "More")
    private Button categoryToggleButton;
    private LinearLayout categorySubLabelsContainer;
    private LinearLayout socialLayout;
    private LinearLayout updatesLayout;
    private LinearLayout forumsLayout;
    private LinearLayout promotionsLayout;

    // Custom labels section
    private ImageView addCustomLabelButton; //
    private LinearLayout customLabelsContainer; // This will hold the dynamic labels

    // Listener for communication with the hosting Activity
    private SideBarFragmentListener listener;

    public interface SideBarFragmentListener {
        void onCategorySelected(String categoryName);
        void onLabelSelected(String labelId, String labelName);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SideBarFragmentListener) {
            listener = (SideBarFragmentListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SideBarFragmentListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sidebar, container, false); //

        // Initialize ViewModels
        mailViewModel = new ViewModelProvider(this).get(MailViewModel.class); //
        labelViewModel = new ViewModelProvider(this).get(LabelViewModel.class); //

        // Initialize UI elements for default categories
        inboxLayout = view.findViewById(R.id.inbox_layout); //
        sentLayout = view.findViewById(R.id.sent_layout); //
        starredLayout = view.findViewById(R.id.starred_layout); //
        draftsLayout = view.findViewById(R.id.drafts_layout); //

        // Initialize UI elements for "More" section
        moreButton = view.findViewById(R.id.more_button); //
        moreLabelsSection = view.findViewById(R.id.more_labels_section); //
        importantLayout = view.findViewById(R.id.important_layout); //
        allMailLayout = view.findViewById(R.id.all_mail_layout); //
        spamLayout = view.findViewById(R.id.more_spam_layout); //
        trashLayout = view.findViewById(R.id.more_trash_layout); //


        // Initialize UI elements for Categories sub-section
        categoryToggleButton = view.findViewById(R.id.category_toggle_button); //
        categorySubLabelsContainer = view.findViewById(R.id.category_sub_labels_container); //
        socialLayout = view.findViewById(R.id.social_layout); //
        updatesLayout = view.findViewById(R.id.updates_layout); //
        forumsLayout = view.findViewById(R.id.forums_layout); //
        promotionsLayout = view.findViewById(R.id.promotions_layout); //

        // Initialize UI elements for custom labels
        addCustomLabelButton = view.findViewById(R.id.add_custom_label_button); //
        customLabelsContainer = view.findViewById(R.id.dynamic_custom_labels_list); //

        if (moreButton != null) { //
            // Set initial drawable for "More" button to arrow_right if section is GONE (collapsed)
            if (moreLabelsSection.getVisibility() == View.GONE) { //
                moreButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_right, 0, 0, 0); //
            } else {
                // If it's visible by default, set it to arrow_down
                moreButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_drop_down, 0, 0, 0); //
            }
        }
        if (categoryToggleButton != null) { //
            // Set initial drawable for "Categories" toggle button to arrow_right if section is GONE (collapsed)
            if (categorySubLabelsContainer.getVisibility() == View.GONE) { //
                categoryToggleButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_right, 0, 0, 0); //
            } else {
                // If it's visible by default, set it to arrow_down
                categoryToggleButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_drop_down, 0, 0, 0); //
            }
        }


        // Set up click listeners for static categories
        if (inboxLayout != null) { //
            inboxLayout.setOnClickListener(v -> { //
                mailViewModel.fetchInboxMails(); //
                if (listener != null) listener.onCategorySelected("Inbox"); //
            });
        }
        if (starredLayout != null) { //
            starredLayout.setOnClickListener(v -> { //
                mailViewModel.fetchStarredMails(); //
                if (listener != null) listener.onCategorySelected("Starred"); //
            });
        }
        if (sentLayout != null) { //
            sentLayout.setOnClickListener(v -> { //
                mailViewModel.fetchSentMails(); //
                if (listener != null) listener.onCategorySelected("Sent"); //
            });
        }
        if (draftsLayout != null) { //
            draftsLayout.setOnClickListener(v -> { //
                mailViewModel.fetchDrafts(); //
                if (listener != null) listener.onCategorySelected("Drafts"); //
            });
        }

        // Set up click listener for "More" button
        if (moreButton != null) { //
            moreButton.setOnClickListener(v -> { //
                if (moreLabelsSection.getVisibility() == View.GONE) { //
                    moreLabelsSection.setVisibility(View.VISIBLE); //
                    // Change drawable to arrow_down when expanded
                    moreButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_drop_down, 0, 0, 0); //
                } else {
                    moreLabelsSection.setVisibility(View.GONE); //
                    // Change drawable back to arrow_right when collapsed
                    moreButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_right, 0, 0, 0); //
                    // Also hide categories if "More" is collapsed
                    categorySubLabelsContainer.setVisibility(View.GONE); //
                    categoryToggleButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_right, 0, 0, 0); //
                }
            });
        }

        // Set up click listeners for the "More" section categories
        if (importantLayout != null) { //
            importantLayout.setOnClickListener(v -> { //
                mailViewModel.fetchImportantMails(); //
                if (listener != null) listener.onCategorySelected("Important"); //
            });
        }
        if (allMailLayout != null) { //
            allMailLayout.setOnClickListener(v -> { //
                mailViewModel.fetchAllMails(); // Use the newly added fetchAllMails
                if (listener != null) listener.onCategorySelected("allmail"); //
            });
        }
        if (spamLayout != null) { //
            spamLayout.setOnClickListener(v -> { //
                mailViewModel.fetchSpamMails(); //
                if (listener != null) listener.onCategorySelected("Spam"); //
            });
        }
        if (trashLayout != null) { //
            trashLayout.setOnClickListener(v -> { //
                mailViewModel.fetchDeletedMails(); //
                if (listener != null) listener.onCategorySelected("Trash"); //
            });
        }


        // Set up click listener for "Categories" toggle button
        if (categoryToggleButton != null) { //
            categoryToggleButton.setOnClickListener(v -> { //
                if (categorySubLabelsContainer.getVisibility() == View.GONE) { //
                    categorySubLabelsContainer.setVisibility(View.VISIBLE); //
                    // Change drawable to arrow_down when expanded
                    categoryToggleButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_drop_down, 0, 0, 0); //
                } else {
                    categorySubLabelsContainer.setVisibility(View.GONE); //
                    // Change drawable back to arrow_right when collapsed
                    categoryToggleButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.arrow_right, 0, 0, 0); //
                }
            });
        }

        // Set up click listeners for category sub-labels
        // These will now find the label ID and call fetchMailsByLabel
        if (socialLayout != null) { //
            socialLayout.setOnClickListener(v -> handleCategoryLabelClick("Social")); //
        }
        if (updatesLayout != null) { //
            updatesLayout.setOnClickListener(v -> handleCategoryLabelClick("Updates")); //
        }
        if (forumsLayout != null) { //
            forumsLayout.setOnClickListener(v -> handleCategoryLabelClick("Forums")); //
        }
        if (promotionsLayout != null) { //
            promotionsLayout.setOnClickListener(v -> handleCategoryLabelClick("Promotions")); //
        }

        // Set up click listener for "Add Label" icon
        if (addCustomLabelButton != null) { //
            addCustomLabelButton.setOnClickListener(v -> { //
                showCreateLabelDialog(); // Call the new method to show the dialog
            });
        }

        // Observe LiveData from MailViewModel to update counts (optional, but good practice)
        // ... (existing count observation code remains the same)

        // Observe LiveData from LabelViewModel to populate custom labels dynamically
        // The displayCustomLabels method will now filter out the predefined categories
        labelViewModel.getLabels().observe(getViewLifecycleOwner(), this::displayCustomLabels); //

        // Fetch initial data when the fragment is created
        mailViewModel.fetchInboxMails(); // Fetch inbox mails on startup
        labelViewModel.fetchLabels(); // Fetch custom labels on startup

        return view; //
    }

    /**
     * Handles clicks on predefined category labels (Social, Updates, etc.).
     * It finds the corresponding Label ID and calls fetchMailsByLabel.
     * @param categoryName The name of the category label clicked.
     */
    private void handleCategoryLabelClick(String categoryName) {
        Log.d("SideBarFragment", "Category selected: " + categoryName);
        // Get all labels observed by the ViewModel
        List<Label> allLabels = labelViewModel.getLabels().getValue(); //
        if (allLabels != null) { //
            for (Label label : allLabels) { //
                if (Objects.equals(label.getName(), categoryName)) { //
                    mailViewModel.fetchMailsByLabel(label.getId()); //
                    if (listener != null) listener.onLabelSelected(label.getId(), label.getName());
                    return;
                }
            }
        }
        Toast.makeText(getContext(), "Category '" + categoryName + "' not found.", Toast.LENGTH_SHORT).show(); //
        Log.e(TAG, "Category label " + categoryName + " not found in fetched labels."); //
    }

    /**
     * Dynamically displays custom labels in the sidebar,
     * filtering out the predefined category labels.
     * @param allLabels The list of all Label objects fetched from the backend.
     */
    private void displayCustomLabels(List<Label> allLabels) {
        if (customLabelsContainer == null) return; //

        customLabelsContainer.removeAllViews(); // Clear existing labels

        // Filter out the predefined category labels
        List<Label> filteredLabels = new ArrayList<>(); //
        for (Label label : allLabels) { //
            if (!CATEGORY_LABEL_NAMES.contains(label.getName())) { //
                filteredLabels.add(label); //
            }
        }

        for (Label label : filteredLabels) { //
            // Create a new layout for each label (e.g., a LinearLayout with a TextView)
            LinearLayout labelView = (LinearLayout) LayoutInflater.from(getContext()) //
                    .inflate(R.layout.item_custom_label, customLabelsContainer, false); // Assuming you have item_custom_label.xml
            TextView labelNameTextView = labelView.findViewById(R.id.label_name); // Assuming ID in item_custom_label.xml
            ImageView labelActionsButton = labelView.findViewById(R.id.label_actions_button); // Assuming ID for a 3-dots menu

            if (labelNameTextView != null) { //
                labelNameTextView.setText(label.getName()); //
            }

            // Set click listener for the label itself to fetch mails by that label
            labelView.setOnClickListener(v -> { //
                mailViewModel.fetchMailsByLabel(label.getId()); // Call the fetch method for labels
                if (listener != null) listener.onLabelSelected(label.getId(), label.getName()); //
            });

            // Set up a click listener for actions (e.g., edit/delete label)
            if (labelActionsButton != null) { //
                labelActionsButton.setOnClickListener(v -> showLabelActionsMenu(v, label)); //
            }

            customLabelsContainer.addView(labelView); //
        }
    }

    /**
     * Shows the Label creation dialog.
     */
    private void showCreateLabelDialog() {
        LabelDialogFragment dialogFragment = new LabelDialogFragment(); // Create new instance
        dialogFragment.setTitle("Create New Label"); // Set title for creation
        dialogFragment.setInitialValue(""); // No initial value for new label
        dialogFragment.setListener(this); // Set this fragment as the listener for onSubmit
        dialogFragment.show(getChildFragmentManager(), "create_label_dialog"); // Show the dialog
    }

    /**
     * Callback from LabelDialogFragment when the user submits a label name.
     * @param labelName The new label name entered by the user.
     */
    @Override
    public void onSubmit(String labelName) { //
        // Here, you would call your LabelViewModel to add the new label
        labelViewModel.createLabel(labelName); // (Assuming your ViewModel has an addLabel method)
        Toast.makeText(getContext(), "Label '" + labelName + "' created!", Toast.LENGTH_SHORT).show(); //
    }

    // Existing showLabelActionsMenu method (from your snippet)
    private void showLabelActionsMenu(View anchorView, Label label) {
        PopupMenu popup = new PopupMenu(getContext(), anchorView); //
        popup.getMenuInflater().inflate(R.menu.label_actions_menu, popup.getMenu()); // Inflate the entire menu XML
        popup.setOnMenuItemClickListener(item -> { //
            int itemId = item.getItemId(); //
            if (itemId == R.id.action_edit_label) { //
                // For editing, you would show the dialog with the existing label's name
                showEditLabelModal(label); // Call existing/new method to show edit dialog
                Toast.makeText(getContext(), "Edit label: " + label.getName(), Toast.LENGTH_SHORT).show(); //
                return true;
            } else if (itemId == R.id.action_delete_label) { // Corrected this line
                labelViewModel.deleteLabel(label.getId()); // Call ViewModel to delete label
                Toast.makeText(getContext(), "Delete label: " + label.getName(), Toast.LENGTH_SHORT).show(); //
                return true;
            }
            return false;
        });
        popup.show(); //
    }

    // New method for showing the edit label dialog
    private void showEditLabelModal(Label labelToEdit) {
        LabelDialogFragment dialogFragment = new LabelDialogFragment(); //
        dialogFragment.setTitle("Edit Label"); //
        dialogFragment.setInitialValue(labelToEdit.getName()); // Pre-fill with current name
        dialogFragment.setListener(newValue -> { //
            // Update the label via ViewModel
            labelToEdit.setName(newValue); // Update the local Label object
            labelViewModel.updateLabel(labelToEdit.getId(), newValue); // Assuming you have an updateLabel method in ViewModel
            Toast.makeText(getContext(), "Label updated to: " + newValue, Toast.LENGTH_SHORT).show(); //
        });
        dialogFragment.show(getChildFragmentManager(), "edit_label_dialog"); //
    }

    // Helper method to convert dp to pixels (if needed, not directly used in this modification)
    private int dpToPx(int dp) { //
        return (int) (dp * getResources().getDisplayMetrics().density); //
    }

    @Override
    public void onDetach() { //
        super.onDetach(); //
        listener = null; //
    }
    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof SideBarFragmentListener) {
            listener = (SideBarFragmentListener) getActivity();
        } else {
            listener = null; // ודאי שהוא null אם האקטיביטי לא מתאים יותר
        }
    }
}