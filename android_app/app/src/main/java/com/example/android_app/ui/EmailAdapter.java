package com.example.android_app.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.android_app.R;
import com.example.android_app.model.Email;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

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

    // Interface for item clicks (regular and long) and star actions
    public interface EmailItemClickListener {
        void onEmailClick(Email email);
        void onEmailLongClick(Email email);
        // Changed to toggle method for simplicity
        // void onToggleEmailStarred(Email email, int position); // Pass position to notify adapter
        void onMarkAsStarred(String mailId);
        void onUnmarkAsStarred(String mailId);
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

        // --- 1. Set basic text fields ---
        holder.subjectTextView.setText(currentEmail.getSubject());

        if (currentEmail.getDate() != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
            holder.textTime.setText(dateFormat.format(currentEmail.getDate()));
        } else {
            holder.textTime.setText("");
        }

        // --- 2. Handle Sender Name & Profile Picture Logic ---
        // Set initial sender name (either actual name, loading text, or error)
        if (currentEmail.getSenderName() != null) {
            holder.senderTextView.setText(currentEmail.getSenderName());
        } else {
            // Show loading text while we wait for the API call to complete
            holder.senderTextView.setText("Unknown Sender");
        }

        // --- 3. Handle Profile Picture (using fromUser) ---
        boolean isSelected = selectedEmailIds.contains(currentEmail.getId());

        // Determine what to show in the image view (profile pic or selection icon)
        if (isMultiSelectMode) {
            holder.imageSenderOrSelected.setVisibility(View.VISIBLE);
            if (isSelected) {
                // Show blue checkmark if item is selected in multi-select mode
                holder.imageSenderOrSelected.setImageResource(R.drawable.ic_check_circle_blue);
                holder.imageSenderOrSelected.setBackgroundResource(0); // Remove any background circle
            } else {
                // Item is not selected but in multi-select mode, show profile pic or placeholder
                loadProfileImage(holder.imageSenderOrSelected, currentEmail.getProfilePicUrl());
            }
        } else {
            // Not in multi-select mode, always show the sender's profile image
            holder.imageSenderOrSelected.setVisibility(View.VISIBLE);
            loadProfileImage(holder.imageSenderOrSelected, currentEmail.getProfilePicUrl());
        }

        // --- 4. Handle Item Background and Activated State (for selector) ---
        holder.itemView.setActivated(isMultiSelectMode && isSelected);

        // --- 5. Handle Read/Unread Status and Text Style ---
        if (currentEmail.isRead()) {
            holder.senderTextView.setTypeface(null, Typeface.NORMAL);
            holder.subjectTextView.setTypeface(null, Typeface.NORMAL);
            holder.textTime.setTypeface(null, Typeface.NORMAL);
            // holder.unreadIndicator.setVisibility(View.GONE);
            holder.senderTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color));
            holder.subjectTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color));
            holder.textTime.setTextColor(ContextCompat.getColor(context, R.color.text_color));
        } else {
            holder.senderTextView.setTypeface(null, Typeface.BOLD);
            holder.subjectTextView.setTypeface(null, Typeface.BOLD);
            holder.textTime.setTypeface(null, Typeface.BOLD);
            // holder.unreadIndicator.setVisibility(View.VISIBLE);
            holder.senderTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color));
            holder.subjectTextView.setTextColor(ContextCompat.getColor(context, R.color.text_color));
            holder.textTime.setTextColor(ContextCompat.getColor(context, R.color.text_color));
        }

        // --- 6. Handle Star Icon (Important/Starred) ---
        if (currentEmail.isStarred()) {
            holder.iconStar.setImageResource(R.drawable.full_star);
        } else {
            holder.iconStar.setImageResource(R.drawable.starred);
        }
        holder.iconStar.setVisibility(View.VISIBLE);
    }

    // --- NEW Helper method for loading profile images ---
    private void loadProfileImage(ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .error(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(imageView);
            imageView.setBackgroundResource(0); // Remove any background circle when image is loaded
        } else {
            imageView.setImageResource(R.drawable.ic_profile_placeholder);
            imageView.setBackgroundResource(R.drawable.circle_background); // Assuming this draws a circle
        }
    }

    @Override
    public int getItemCount() {
        return emailList != null ? emailList.size() : 0;
    }

    public void setEmails(List<Email> newEmails) {
        this.emailList = newEmails;
        if (!isMultiSelectMode) {
            selectedEmailIds.clear(); // Clear selection if not in multi-select
        } else {
            // Re-validate existing selections if new emails list came in while in multi-select
            selectedEmailIds.retainAll(getEmailIds(newEmails));
            updateMultiSelectMode();
        }
        notifyDataSetChanged(); // Refresh the entire view
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
        int position = emailList.indexOf(email);
        if (position != -1) {
            notifyItemChanged(position); // Notify specific item changed
        }
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
            setMultiSelectMode(false); // This will call notifyDataSetChanged()
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
    // Make this class public static to avoid potential memory leak warnings in some lint checks
    public class EmailViewHolder extends RecyclerView.ViewHolder {
        LinearLayout emailItemContainer; // Use this for the item's background and click events
        TextView senderTextView;
        TextView subjectTextView;
        TextView textTime;
        ImageView imageSenderOrSelected;
        View unreadIndicator;
        ImageView iconStar;

        public EmailViewHolder(@NonNull View itemView) {
            super(itemView);
            emailItemContainer = itemView.findViewById(R.id.emailItemContainer); // Assuming this is the root LinearLayout
            senderTextView = itemView.findViewById(R.id.senderTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            textTime = itemView.findViewById(R.id.textTime);
            imageSenderOrSelected = itemView.findViewById(R.id.imageSenderOrSelected);
            unreadIndicator = itemView.findViewById(R.id.unreadIndicator);
            iconStar = itemView.findViewById(R.id.iconStar);

            // --- Set Click Listeners Here (Crucial for interaction) ---
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Email clickedEmail = emailList.get(position);

                    if (isMultiSelectMode) {
                        toggleEmailSelection(clickedEmail);
                    } else {
                        if (itemClickListener != null) {
                            itemClickListener.onEmailClick(clickedEmail);
                            // After clicking an email in normal mode, mark it as read.
                            // Assuming you have a way to update the 'read' status in your model/DB.
                            // For immediate UI update, toggle isRead and notifyItemChanged.
                            if (!clickedEmail.isRead()) {
                                clickedEmail.setIsRead(true); // Update data model
                                notifyItemChanged(position); // Refresh this item's view
                            }
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Email longClickedEmail = emailList.get(position);

                    if (!isMultiSelectMode) {
                        setMultiSelectMode(true); // Enter multi-select mode
                    }
                    toggleEmailSelection(longClickedEmail); // Select the item that was long-clicked

                    if (itemClickListener != null) {
                        itemClickListener.onEmailLongClick(longClickedEmail); // Notify Activity of long click
                    }
                    return true; // Consume the long click event
                }
                return false; // Did not consume
            });

            iconStar.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && itemClickListener != null) {
                    Email starredEmail = emailList.get(position);
                    boolean newStarredStatus = !starredEmail.isStarred();
                    starredEmail.setStarred(newStarredStatus);

                    if (newStarredStatus) {
                        itemClickListener.onMarkAsStarred(starredEmail.getId());
                    } else {
                        itemClickListener.onUnmarkAsStarred(starredEmail.getId());
                    }
                    notifyItemChanged(position);
                }

            });
        }

        // bind method is mostly for setting text/image resources based on email data
        void bind(Email email) {
            senderTextView.setText(email.getFrom());
            subjectTextView.setText(email.getSubject());

            if (email.getDate() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                textTime.setText(dateFormat.format(email.getDate()));
            } else {
                textTime.setText("");
            }
            // The imageSenderOrSelected and iconStar are set in onBindViewHolder due to conditional logic
            // The background is also set in onBindViewHolder based on multi-select/read state
        }
    }
}