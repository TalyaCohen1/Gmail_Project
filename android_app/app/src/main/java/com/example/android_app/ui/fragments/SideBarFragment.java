package com.example.android_app.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.example.android_app.data.network.LabelService;
import com.example.android_app.model.Email;
import com.example.android_app.model.Label;
import com.example.android_app.model.viewmodel.LabelViewModel;
import com.example.android_app.model.viewmodel.MailViewModel;
import com.example.android_app.utils.ViewModelFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SideBarFragment extends Fragment {

    private MailViewModel mailViewModel;
    private LabelViewModel labelViewModel; // Assuming you have a LabelViewModel for label management

    // Declare your LinearLayouts/TextViews for the categories here (example)
    private LinearLayout inboxLayout;
    private LinearLayout sentLayout;
    private LinearLayout draftsLayout;
    private LinearLayout spamLayout;
    private LinearLayout trashLayout;

    private LinearLayout customLabelsContainer; // Container for dynamically added custom labels

    // Listener for communication with the hosting Activity
    private SideBarFragmentListener listener;

    public interface SideBarFragmentListener {
        void onCategorySelected(String categoryName);
        void onLabelSelected(String labelId, String labelName);
        // Add other methods as needed, e.g., for compose email
        void onComposeEmailSelected();
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
        View view = inflater.inflate(R.layout.fragment_sidebar, container, false);

        // Initialize ViewModelFactory and ViewModels
        mailViewModel = new ViewModelProvider(this).get(MailViewModel.class); // No custom factory needed here
        labelViewModel = new ViewModelProvider(this).get(LabelViewModel.class); // No custom factory needed here


        // Initialize UI elements (replace with your actual IDs)
        inboxLayout = view.findViewById(R.id.inbox_layout); // Example ID
        sentLayout = view.findViewById(R.id.sent_layout);
        draftsLayout = view.findViewById(R.id.drafts_layout);
        spamLayout = view.findViewById(R.id.spam_layout);
        trashLayout = view.findViewById(R.id.trash_layout);
        customLabelsContainer = view.findViewById(R.id.custom_labels_container); // Example ID for a LinearLayout

        // Set up click listeners for static categories
        if (inboxLayout != null) {
            inboxLayout.setOnClickListener(v -> {
                mailViewModel.fetchInboxMails(); // Call the fetch method
                if (listener != null) listener.onCategorySelected("Inbox");
            });
        }
        if (sentLayout != null) {
            sentLayout.setOnClickListener(v -> {
                mailViewModel.fetchSentMails(); // Call the fetch method
                if (listener != null) listener.onCategorySelected("Sent");
            });
        }
        if (draftsLayout != null) {
            draftsLayout.setOnClickListener(v -> {
                mailViewModel.fetchDrafts(); // Call the fetch method
                if (listener != null) listener.onCategorySelected("Drafts");
            });
        }
        if (spamLayout != null) {
            spamLayout.setOnClickListener(v -> {
                mailViewModel.fetchSpamMails(); // Call the fetch method
                if (listener != null) listener.onCategorySelected("Spam");
            });
        }
        if (trashLayout != null) {
            trashLayout.setOnClickListener(v -> {
                mailViewModel.fetchDeletedMails(); // Call the fetch method
                if (listener != null) listener.onCategorySelected("Trash");
            });
        }


        // Observe LiveData from MailViewModel to update counts (optional, but good practice)
        mailViewModel.getInboxMails().observe(getViewLifecycleOwner(), emails -> {
            // Update inbox count TextView if you have one
            // TextView inboxCount = view.findViewById(R.id.inbox_count);
            // if (inboxCount != null) inboxCount.setText(String.valueOf(emails.size()));
        });
        // Repeat for other categories (sent, drafts, etc.)

        // Observe LiveData from LabelViewModel to populate custom labels dynamically
        labelViewModel.getLabels().observe(getViewLifecycleOwner(), this::displayCustomLabels);

        // Fetch initial data when the fragment is created
        mailViewModel.fetchInboxMails(); // Fetch inbox mails on startup
        labelViewModel.fetchLabels(); // Fetch custom labels on startup

        return view;
    }

    /**
     * Dynamically displays custom labels in the sidebar.
     * You will need to create a layout for each label (e.g., a LinearLayout containing a TextView).
     * @param labels The list of custom Label objects.
     */
    private void displayCustomLabels(List<Label> labels) {
        if (customLabelsContainer == null) return;

        customLabelsContainer.removeAllViews(); // Clear existing labels

        for (Label label : labels) {
            // Create a new layout for each label (e.g., a LinearLayout with a TextView)
            LinearLayout labelView = (LinearLayout) LayoutInflater.from(getContext())
                    .inflate(R.layout.item_custom_label, customLabelsContainer, false); // Create item_sidebar_label.xml if you don't have it
            TextView labelNameTextView = labelView.findViewById(R.id.label_name); // Assuming ID in item_sidebar_label.xml
            ImageView labelActionsButton = labelView.findViewById(R.id.label_actions_button); // Assuming ID for a 3-dots menu

            if (labelNameTextView != null) {
                labelNameTextView.setText(label.getName());
            }

            // Set click listener for the label itself to fetch mails by that label
            labelView.setOnClickListener(v -> {
                mailViewModel.fetchMailsByLabel(label.getId()); // Call the fetch method for labels
                if (listener != null) listener.onLabelSelected(label.getId(), label.getName());
            });

            // Set up a click listener for actions (e.g., edit/delete label)
            if (labelActionsButton != null) {
                labelActionsButton.setOnClickListener(v -> showLabelActionsMenu(v, label));
            }

            customLabelsContainer.addView(labelView);
        }
    }

    // Existing showLabelActionsMenu method (from your snippet)
    private void showLabelActionsMenu(View anchorView, Label label) {
        PopupMenu popup = new PopupMenu(getContext(), anchorView);
        popup.getMenuInflater().inflate(R.menu.label_actions_menu, popup.getMenu()); // Inflate the entire menu XML
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_edit_label) {
                // showEditLabelModal(label); // Assuming you have this method
                Toast.makeText(getContext(), "Edit label: " + label.getName(), Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.action_delete_label) {
                labelViewModel.deleteLabel(label.getId()); // Call ViewModel to delete label
                Toast.makeText(getContext(), "Delete label: " + label.getName(), Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        popup.show();
    }


    // Helper method to convert dp to pixels
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}