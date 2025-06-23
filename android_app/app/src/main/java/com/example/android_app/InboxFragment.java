package com.example.android_app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;

public class InboxFragment extends Fragment {

    public InboxFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Load the xml of the fragment
        return inflater.inflate(R.layout.fragment_inbox, container, false);
    }
}
