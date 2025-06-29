package com.example.android_app.ui.fragments;

import android.app.Dialog; // Import for Dialog
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog; // For building a standard dialog
import androidx.fragment.app.DialogFragment; // IMPORTANT: Extend DialogFragment

import com.example.android_app.R; // Ensure your R file is correctly imported

/**
 * A reusable DialogFragment for creating or editing labels.
 * It provides an input field for the label name and "Save" / "Cancel" buttons.
 * The calling Fragment (e.g., SidebarFragment) can customize its title,
 * initial input value, and provide a listener for the "Save" action.
 */
public class LabelDialogFragment extends DialogFragment {

    // --- Private fields to store configuration passed from the caller ---
    private String title;
    private String initialValue;
    private LabelModalListener listener; // This is the callback for when the user clicks "Save"

    /**
     * Interface to communicate the user's input back to the calling Fragment.
     * The calling Fragment must implement this or provide a lambda.
     */
    public interface LabelModalListener {
        /**
         * Called when the user clicks the "Save" button and the input is not empty.
         * @param value The trimmed text entered by the user in the input field.
         */
        void onSubmit(String value);
    }

    // --- Public setter methods to configure the dialog from the calling Fragment ---

    /**
     * Sets the title to be displayed at the top of the dialog.
     * @param title The desired title string.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Sets an initial value for the input EditText.
     * Useful for "Edit" mode where you pre-fill the existing label name.
     * @param initialValue The string to pre-fill the input field with.
     */
    public void setInitialValue(String initialValue) {
        this.initialValue = initialValue;
    }

    /**
     * Sets the listener that will be called when the "Save" button is clicked.
     * The listener will receive the user-entered value.
     * @param listener An implementation of LabelModalListener (often a lambda).
     */
    public void setListener(LabelModalListener listener) {
        this.listener = listener;
    }

    /**
     * This is the core method for creating the Dialog itself.
     * It builds an AlertDialog and sets its custom content view.
     *
     * @param savedInstanceState A Bundle containing the fragment's previously saved state.
     * @return A new Dialog instance to be displayed by the DialogFragment.
     */
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Create a new AlertDialog.Builder to construct the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        // Get the LayoutInflater to inflate the custom layout for the dialog's content
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate the custom layout (dialog_label_modal.xml) for the dialog's main area
        // The 'null' parameter for root means it will be attached to the dialog's root internally
        View view = inflater.inflate(R.layout.fragment_label_dialog, null);

        // Find UI elements from the inflated 'dialog_label_modal.xml' layout
        TextView dialogTitle = view.findViewById(R.id.dialog_title);
        EditText labelInput = view.findViewById(R.id.label_input);
        Button saveButton = view.findViewById(R.id.save_button);
        Button cancelButton = view.findViewById(R.id.cancel_button);

        // --- Configure the dialog's UI based on passed parameters ---

        // Set the dialog title. Use a default if no title was provided.
        dialogTitle.setText(title != null ? title : "Label");

        // Set the initial value of the input field.
        if (initialValue != null) {
            labelInput.setText(initialValue);
            // Move cursor to the end of the pre-filled text for convenience
            labelInput.setSelection(initialValue.length());
        }

        // --- Set up button click listeners ---

        // Listener for the "Save" button
        saveButton.setOnClickListener(v -> {
            String value = labelInput.getText().toString().trim(); // Get trimmed input
            if (value.isEmpty()) {
                // Show a toast if the input is empty
                Toast.makeText(getContext(), "Label name cannot be empty.", Toast.LENGTH_SHORT).show();
            } else if (listener != null) {
                // If a listener is set and input is valid, notify the listener
                listener.onSubmit(value);
                dismiss(); // Dismiss the dialog after a successful submission
            }
        });

        // Listener for the "Cancel" button
        cancelButton.setOnClickListener(v -> dismiss()); // Simply dismiss the dialog

        // --- Set the custom view to the AlertDialog builder and create the dialog ---
        builder.setView(view);

        // Create and return the configured AlertDialog
        return builder.create();
    }

    // No need for onCreateView here, as onCreateDialog is used to construct the Dialog directly.
    // If you were creating a full-screen or custom-shaped dialog that behaves more like a fragment
    // with its own content view, then onCreateView would be used.
}
