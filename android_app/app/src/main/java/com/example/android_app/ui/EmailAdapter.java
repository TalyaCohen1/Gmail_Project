package com.example.android_app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface; // Import for Typeface
import android.util.Log; // Import for Log
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // Import for ImageView
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.R;
import com.example.android_app.model.Email;
import com.example.android_app.ui.EmailDetailsActivity; // Make sure this import is correct

import java.util.ArrayList; // Import for ArrayList
import java.util.HashSet; // Import for HashSet
import java.util.List;
import java.util.Set; // Import for Set

public class EmailAdapter extends RecyclerView.Adapter<EmailAdapter.EmailViewHolder> {

    private final Context context;
    private List<Email> emailList;
    private final Set<String> selectedEmailIds; // Keeps track of selected email IDs
    private boolean isMultiSelectMode = false;
    private MultiSelectModeListener multiSelectModeListener; // Listener for activity callbacks
    private EmailItemClickListener itemClickListener; // Listener for regular/long clicks

    // Interface for communicating multi-select state to the Activity
    public interface MultiSelectModeListener {
        void onMultiSelectModeChanged(boolean inMultiSelectMode);
        void onSelectedCountChanged(int count);
    }

    // Interface for item clicks (regular and long)
    public interface EmailItemClickListener {
        void onEmailClick(Email email);
        void onEmailLongClick(Email email);
    }

    // Updated constructor to accept the click listener
    public EmailAdapter(Context context, List<Email> emailList, EmailItemClickListener listener) {
        this.context = context;
        this.emailList = emailList;
        this.itemClickListener = listener;
        this.selectedEmailIds = new HashSet<>();
    }

    public void setMultiSelectModeListener(MultiSelectModeListener listener) {
        this.multiSelectModeListener = listener;
    }

    @NonNull
    @Override
    public EmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_email, parent, false);
        return new EmailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EmailViewHolder holder, int position) {
        Email currentEmail = emailList.get(position);
        holder.bind(currentEmail);

        boolean isSelected = selectedEmailIds.contains(currentEmail.getId());

        // Visually mark selected items (background color)
        holder.itemView.setBackgroundResource(isSelected ? R.color.selected_item_background : R.color.white);

        // Handle image based on multi-select mode and selection state
        if (isMultiSelectMode) {
            // In multi-select mode, show a checkmark or initial based on selection
            if (isSelected) {
                holder.imageSenderOrSelected.setImageResource(R.drawable.ic_check_circle_blue);
                holder.imageSenderOrSelected.setBackgroundResource(0); // Remove circle background
            } else {
                // Show initial of sender name in a circle if not selected
                String senderName = currentEmail.getFrom(); // Assuming getFrom() gives the sender's display name
                if (senderName != null && !senderName.isEmpty()) {
                    char initial = senderName.toUpperCase().charAt(0);
                    // This part is tricky. Drawing text on a circle is not straightforward with ImageView alone.
                    // For now, we'll use a placeholder or simply the default profile icon
                    // You might need a custom Drawable or a library like 'TextDrawable' for proper initials.
                    // For simplicity, let's just show the default placeholder or draw it programmatically (more complex).
                    holder.imageSenderOrSelected.setImageResource(0); // Clear existing image
                    holder.imageSenderOrSelected.setBackgroundResource(R.drawable.circle_background); // Re-apply circle shape for background
                    // To show initials, you would need to set a TextDrawable or draw on a Canvas here.
                    // Example (if using a TextDrawable-like approach):
                    // holder.imageSenderOrSelected.setImageDrawable(TextDrawable.builder().buildRound(String.valueOf(initial), Color.GRAY));
                } else {
                    holder.imageSenderOrSelected.setImageResource(R.drawable.ic_profile_placeholder);
                    holder.imageSenderOrSelected.setBackgroundResource(R.drawable.circle_background);
                }
            }
        } else {
            // Not in multi-select mode, show default sender image (e.g., initial or profile pic placeholder)
            holder.imageSenderOrSelected.setImageResource(R.drawable.ic_profile_placeholder);
            holder.imageSenderOrSelected.setBackgroundResource(R.drawable.circle_background);
        }

        // Update read/unread indicator and text style
        if (currentEmail.isRead()) { // Assuming Email model has isRead() method
            holder.senderTextView.setTypeface(null, Typeface.NORMAL);
            holder.subjectTextView.setTypeface(null, Typeface.NORMAL);
            holder.unreadIndicator.setVisibility(View.GONE);
        } else {
            holder.senderTextView.setTypeface(null, Typeface.BOLD);
            holder.subjectTextView.setTypeface(null, Typeface.BOLD);
            holder.unreadIndicator.setVisibility(View.VISIBLE);
        }

        // Set click listeners
        holder.itemView.setOnClickListener(v -> {
            if (isMultiSelectMode) {
                toggleEmailSelection(currentEmail);
            } else {
                if (itemClickListener != null) {
                    itemClickListener.onEmailClick(currentEmail);
                } else {
                    // Original navigation logic if no specific itemClickListener is set for normal mode
                    Intent intent = new Intent(context, EmailDetailsActivity.class);
                    intent.putExtra("email_id", currentEmail.getId());
                    context.startActivity(intent);
                }
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (!isMultiSelectMode) {
                setMultiSelectMode(true); // Enter multi-select mode
            }
            toggleEmailSelection(currentEmail); // Select the item that was long-clicked
            if (itemClickListener != null) {
                itemClickListener.onEmailLongClick(currentEmail); // Notify Activity of long click
            }
            return true; // Consume the long click event
        });
    }

    @Override
    public int getItemCount() {
        return emailList != null ? emailList.size() : 0;
    }

    // Method to update the list when data arrives from the ViewModel
    public void setEmails(List<Email> newEmails) {
        this.emailList = newEmails;
        // When setting new emails, ensure selection state is handled correctly.
        // If exiting multi-select, clear selectedEmailIds.
        // If remaining in multi-select (e.g., after an action), might need to re-validate selections.
        if (!isMultiSelectMode) { // Clear selection only if not actively in multi-select mode
            selectedEmailIds.clear();
        } else {
            // Re-validate existing selections if new emails list came in while in multi-select
            // This is important if emails were deleted/moved.
            selectedEmailIds.retainAll(getEmailIds(newEmails)); // Remove IDs that are no longer in the new list
            updateMultiSelectMode(); // Update count and mode if needed
        }
        notifyDataSetChanged(); // Refresh the view
    }

    private Set<String> getEmailIds(List<Email> emails) {
        Set<String> ids = new HashSet<>();
        for (Email email : emails) {
            ids.add(email.getId());
        }
        return ids;
    }


    public List<Email> getSelectedEmails() {
        List<Email> selected = new ArrayList<>();
        for (Email email : emailList) {
            if (selectedEmailIds.contains(email.getId())) {
                selected.add(email);
            }
        }
        return selected;
    }

    public void clearSelection() {
        selectedEmailIds.clear();
        setMultiSelectMode(false); // Exit multi-select mode when clearing selection
    }

    public void toggleEmailSelection(Email email) {
        if (selectedEmailIds.contains(email.getId())) {
            selectedEmailIds.remove(email.getId());
        } else {
            selectedEmailIds.add(email.getId());
        }
        notifyItemChanged(emailList.indexOf(email)); // Notify specific item changed
        updateMultiSelectMode(); // Update mode and count
    }

    private void updateMultiSelectMode() {
        boolean newMultiSelectMode = !selectedEmailIds.isEmpty();
        if (newMultiSelectMode != isMultiSelectMode) {
            setMultiSelectMode(newMultiSelectMode);
        }
        if (multiSelectModeListener != null) {
            multiSelectModeListener.onSelectedCountChanged(selectedEmailIds.size());
        }
        if (selectedEmailIds.isEmpty() && isMultiSelectMode) { // Exit multi-select if nothing is selected
            setMultiSelectMode(false);
        }
    }

    public boolean isMultiSelectMode() {
        return isMultiSelectMode;
    }

    public void setMultiSelectMode(boolean multiSelectMode) {
        if (this.isMultiSelectMode != multiSelectMode) {
            this.isMultiSelectMode = multiSelectMode;
            if (!multiSelectMode) { // If exiting multi-select mode, ensure selection is cleared
                selectedEmailIds.clear(); // Important to clear selection on exit
                if (multiSelectModeListener != null) {
                    multiSelectModeListener.onSelectedCountChanged(0);
                }
            }
            if (multiSelectModeListener != null) {
                multiSelectModeListener.onMultiSelectModeChanged(multiSelectMode);
            }
            notifyDataSetChanged(); // Important: Redraw all items to reflect mode change
        }
    }

    // The ViewHolder that holds the components of each row
    // Make this class public static if it's not nested in EmailAdapter, or keep as is.
    class EmailViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        TextView subjectTextView;
        TextView textTime; // New: for email time
        ImageView imageSenderOrSelected; // New: for sender image or selection indicator
        View unreadIndicator; // New: for unread dot

        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            textTime = itemView.findViewById(R.id.textTime); // Initialize new view
            imageSenderOrSelected = itemView.findViewById(R.id.imageSenderOrSelected); // Initialize new view
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator); // Initialize new view
        }

        void bind(Email email) {
            senderTextView.setText(email.getFrom());
            subjectTextView.setText(email.getSubject());
            textTime.setText(email.getDate()); // Placeholder for now
            // Ensure your Email model has a 'isRead()' method
            // and a 'getId()' method
        }
    }
}