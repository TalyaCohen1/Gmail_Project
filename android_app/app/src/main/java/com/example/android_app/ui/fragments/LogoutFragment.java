package com.example.android_app.ui.fragments;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.android_app.R;
import com.example.android_app.ui.activity.LoginActivity;
import com.example.android_app.utils.SharedPrefsManager;


public class LogoutFragment extends Fragment{
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logout, container, false);

        Button logoutButton = view.findViewById(R.id.button_logout);
        logoutButton.setOnClickListener(v -> performLogout());

        return view;
    }

    private void performLogout() {
        SharedPrefsManager.clearAll(requireContext());

        //navigate to login page
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
