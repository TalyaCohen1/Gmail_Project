package com.example.android_app.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.android_app.BuildConfig;
import com.example.android_app.R;
import com.example.android_app.model.viewmodel.EditProfileViewModel;
import com.example.android_app.utils.ViewModelFactory;
import com.example.android_app.utils.UserManager;

public class EditProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 101;

    private EditText editTextName;
    private ImageView imageProfile;
    private Button buttonSave;
    private TextView textGreeting, textError, textSuccess;
    private Uri selectedImageUri;
    private EditProfileViewModel viewModel;

    private OnProfilePictureUpdatedListener listener; // Declare the listener variable

    // Method to set the listener
    public void setOnProfilePictureUpdatedListener(OnProfilePictureUpdatedListener listener) {
        this.listener = listener;
    }
    // Inside EditProfileFragment.java, outside the class, or as a nested public interface
    public interface OnProfilePictureUpdatedListener {
        void onProfilePictureUpdated();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_profile, container, false);

        //get all the ui components
        editTextName = view.findViewById(R.id.editTextName);
        imageProfile = view.findViewById(R.id.imageProfile);
        buttonSave = view.findViewById(R.id.buttonSave);
        textGreeting = view.findViewById(R.id.textGreeting);
        textError = view.findViewById(R.id.textError);
        textSuccess = view.findViewById(R.id.textSuccess);

        // create viewmodel instance
        ViewModelFactory factory = new ViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(this, factory).get(EditProfileViewModel.class);
        String fullName = UserManager.getFullName(requireContext());
        String profileImageUrl = UserManager.getProfileImage(requireContext());
        editTextName.setText(fullName);
        textGreeting.setText("Hello, " + fullName);

        //show userProfile image if exist
        if (profileImageUrl != null) {
            // Use Glide to load the profile image
            String fullUrl;
            if (!profileImageUrl.startsWith("http") && !profileImageUrl.startsWith("content://") && !profileImageUrl.startsWith("file://")) {
                fullUrl = BuildConfig.SERVER_URL + profileImageUrl;
            } else {
                fullUrl = profileImageUrl;
            }
            Glide.with(this)
                    .load(fullUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(imageProfile);
        } else {
            // If no profile image URL, set default
            imageProfile.setImageResource(R.drawable.default_profile);
        }


        // open galery to choose profile image
        imageProfile.setOnClickListener(v -> selectImageFromGallery());

        // save button click to update profile
        buttonSave.setOnClickListener(v -> {
            String newName = editTextName.getText().toString().trim();
            if (newName.isEmpty()) {
                textError.setText("Name cannot be empty");
                textError.setVisibility(View.VISIBLE);
                textSuccess.setVisibility(View.GONE);
                return;
            }
            viewModel.updateProfile(newName, selectedImageUri);
        });

        //see error message if failed
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                textError.setText(error);
                textError.setVisibility(View.VISIBLE);
                textSuccess.setVisibility(View.GONE);
            }
        });

        //see success message if success
        viewModel.getSuccessMessage().observe(getViewLifecycleOwner(), success -> {
            if (success != null) {
                textSuccess.setText(success);
                textSuccess.setVisibility(View.VISIBLE);
                textError.setVisibility(View.GONE);
                if (listener != null) {
                    android.util.Log.d("EditProfileFragment", "Notifying listener of profile picture update.");
                    listener.onProfilePictureUpdated(); // <--- THIS IS CRUCIAL!
                } else {
                    android.util.Log.w("EditProfileFragment", "Listener is null, cannot notify profile picture update.");
                }
                // Update greeting and profile image immediately after successful save
                String updatedFullName = UserManager.getFullName(requireContext());
                String updatedProfileImageUrl = UserManager.getProfileImage(requireContext());
                textGreeting.setText("Hello, " + updatedFullName);
                if (updatedProfileImageUrl != null) {
                    String fullUrl;
                    if (!updatedProfileImageUrl.startsWith("http") && !updatedProfileImageUrl.startsWith("content://") && !updatedProfileImageUrl.startsWith("file://")) {
                        fullUrl = BuildConfig.SERVER_URL + updatedProfileImageUrl;
                    } else {
                        fullUrl = updatedProfileImageUrl;
                    }
                    Glide.with(this)
                            .load(fullUrl)
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .circleCrop()
                            .into(imageProfile);
                } else {
                    imageProfile.setImageResource(R.drawable.default_profile);
                }
//                // Notify the listener that the profile picture has been updated
//                if (listener != null) {
//                    listener.onProfilePictureUpdated();
//                }
            }
            new android.os.Handler().postDelayed(() -> {
                requireActivity().getSupportFragmentManager().popBackStack();
            }, 500);
        });

        return view;
    }

    //select image from gallery to update profile
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    //get image from gallery
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            imageProfile.setImageURI(selectedImageUri);
        }
    }
    //load image from url
    private void loadImageFromUrl(String urlString, ImageView imageView) {
        new Thread(() -> {
            try {
                java.net.URL url = new java.net.URL(urlString);
                java.io.InputStream input = url.openStream();
                android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);
                requireActivity().runOnUiThread(() -> imageView.setImageBitmap(bitmap));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

}
